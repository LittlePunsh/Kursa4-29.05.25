package org.kursplom.dataloader;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.kursplom.model.*;
import org.kursplom.repository.*;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.text.Normalizer;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.LinkedHashMap;

@Component
public class DataLoader implements CommandLineRunner {
    private static final Logger logger = LoggerFactory.getLogger(DataLoader.class);

    private final GameRepository gameRepository;
    private final DecisionTypeRepository decisionTypeRepository;
    private final DecisionPointRepository decisionPointRepository;
    private final DecisionChoiceRepository decisionChoiceRepository;
    private final SaveConfigurationRepository saveConfigurationRepository;
    private final ConfigurationChoiceRepository configurationChoiceRepository;
    private final PremadeSaveRepository premadeSaveRepository;
    private final UserDecisionRepository userDecisionRepository;

    private final ObjectMapper objectMapper;
    private final ResourceLoader resourceLoader;

    @Value("${app.dataloader.witcher1-global-saves-json:classpath:data/witcher1_global_saves_data.json}")
    private String globalSavesDataFilePath;

    @Value("${app.dataloader.witcher1-local-decisions-json:classpath:data/witcher1_local_decisions.json}")
    private String localDecisionsDataFilePath;

    private Map<String, String> globalSaveNameToFileMap;
    private Map<String, DecisionPoint> pointsByIdentifierCache;
    private Map<String, DecisionChoice> choicesByIdentifierCache;

    private Map<String, DecisionMappingInfo> decisionMapping;
    private Map<String, LinkedHashMap<String, List<String>>> witcher1ChronologicalOrder;


    public DataLoader(GameRepository gameRepository,
                      DecisionTypeRepository decisionTypeRepository,
                      DecisionPointRepository decisionPointRepository,
                      DecisionChoiceRepository decisionChoiceRepository,
                      SaveConfigurationRepository saveConfigurationRepository,
                      ConfigurationChoiceRepository configurationChoiceRepository,
                      PremadeSaveRepository premadeSaveRepository,
                      UserDecisionRepository userDecisionRepository,
                      ObjectMapper objectMapper,
                      ResourceLoader resourceLoader) {
        this.gameRepository = gameRepository;
        this.decisionTypeRepository = decisionTypeRepository;
        this.decisionPointRepository = decisionPointRepository;
        this.decisionChoiceRepository = decisionChoiceRepository;
        this.saveConfigurationRepository = saveConfigurationRepository;
        this.configurationChoiceRepository = configurationChoiceRepository;
        this.premadeSaveRepository = premadeSaveRepository;
        this.userDecisionRepository = userDecisionRepository;
        this.objectMapper = objectMapper;
        this.resourceLoader = resourceLoader;

        initializeFilenameMapping(); // Сопоставление имен файлов
        initializeChronologicalOrder(); // Хронологический порядок
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        logger.info("DataLoader running...");

        Optional<Game> w1GameOptional = gameRepository.findByName("The Witcher");
        // Проверка, есть ли игра Ведьмак 1 и есть ли у нее точки решения
        if (w1GameOptional.isPresent() && decisionPointRepository.countByGame(w1GameOptional.get()) > 0) {
            logger.info("Database already contains Witcher 1 data. Skipping initial data loading.");
        } else {
            logger.info("Database is empty or Witcher 1 data is incomplete. Loading initial data.");
            if (w1GameOptional.isPresent()) {
                logger.warn("Existing Witcher 1 game found but no decision points. Clearing related data for a fresh load.");
                Game w1GameForClear = w1GameOptional.get();
                userDecisionRepository.deleteByDecisionPointGame(w1GameForClear);
                premadeSaveRepository.deleteByGame(w1GameForClear);
                configurationChoiceRepository.deleteByConfigurationOriginatingGame(w1GameForClear);
                saveConfigurationRepository.deleteByOriginatingGame(w1GameForClear);
                decisionPointRepository.deleteByGame(w1GameForClear);
                logger.info("Cleared existing Witcher 1 related data.");
            }

            loadBaseData();
            initializeDecisionMapping();

            // Получаем сущности игр и типов решения
            Game w1Game = gameRepository.findByName("The Witcher").orElseThrow(() -> new RuntimeException("Witcher 1 not found!"));
            Game w2Game = gameRepository.findByName("The Witcher 2: Assassins of Kings").orElseThrow(() -> new RuntimeException("Witcher 2 not found!"));
            DecisionType globalType = decisionTypeRepository.findByName("GLOBAL").orElseThrow(() -> new RuntimeException("GLOBAL type not found!"));
            DecisionType localType = decisionTypeRepository.findByName("LOCAL").orElseThrow(() -> new RuntimeException("LOCAL type not found!"));


            pointsByIdentifierCache = new HashMap<>();
            choicesByIdentifierCache = new HashMap<>();

            List<SaveDataJsonEntry> globalSaveEntries = loadJsonData(globalSavesDataFilePath);
            processWitcher1Data(globalSaveEntries, w1Game);
            logger.info("Finished processing GLOBAL decisions from JSON.");

            List<SaveDataJsonEntry> localDecisionEntries = loadJsonData(localDecisionsDataFilePath);
            processWitcher1Data(localDecisionEntries, w1Game);
            logger.info("Finished processing LOCAL decisions from JSON.");


            assignChronologicalOrderToPoints();
            logger.info("Finished assigning chronological order to DecisionPoints.");


            processWitcher1GlobalConfigs(globalSaveEntries, w1Game, w2Game);
            logger.info("Finished processing global save configurations and premade saves.");
        }
        logger.info("DataLoader finished.");
    }

    private List<SaveDataJsonEntry> loadJsonData(String filePath) throws IOException {
        logger.info("Loading JSON data from: {}", filePath);
        Resource resource = resourceLoader.getResource(filePath);
        if (!resource.exists()) {
            logger.error("JSON file not found: {}", filePath);
            throw new IOException("JSON file not found: " + filePath);
        }
        try (InputStream inputStream = resource.getInputStream()) {
            List<SaveDataJsonEntry> entries = objectMapper.readValue(
                    inputStream,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, SaveDataJsonEntry.class)
            );
            logger.info("Successfully read {} entries from {}", entries.size(), filePath);
            return entries;
        }
    }

    private static class ParsedChoiceData {
        String chapterName;
        String questName;
        String choiceText;
        String consequenceText;
        String rawPointText;
        boolean isValid = false;

        private static final Pattern CHOICE_STRING_PATTERN = Pattern.compile("(.+?) — (.+?): (.+?) — (.+)");

        public static ParsedChoiceData parse(String choiceString) {
            ParsedChoiceData data = new ParsedChoiceData();
            Matcher matcher = CHOICE_STRING_PATTERN.matcher(choiceString);

            if (matcher.matches()) {
                data.chapterName = matcher.group(1).trim();
                data.questName = matcher.group(2).trim();
                data.choiceText = matcher.group(3).trim();
                data.consequenceText = matcher.group(4).trim();
                data.rawPointText = data.chapterName + " — " + data.questName + ": " + data.choiceText;

                if (!data.chapterName.isEmpty() && !data.questName.isEmpty() && !data.choiceText.isEmpty()) {
                    data.isValid = true;
                } else {
                    logger.warn("Parsed parts contain empty strings after parsing: {}", choiceString);
                }
            } else {
                logger.warn("Failed to match pattern for choice string: {}", choiceString);
            }
            return data;
        }

        public boolean isValid() {
            return this.isValid;
        }

        private static String normalizeToIdentifierPart(String input) {
            if (input == null || input.isEmpty()) return "UNKNOWN";
            String normalized = Normalizer.normalize(input, Normalizer.Form.NFD)
                    .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                    .toLowerCase()
                    .replaceAll("[^a-z0-9а-яА-Я\\s]+", "")
                    .trim()
                    .replaceAll("\\s+", "_");
            return normalized.isEmpty() ? "EMPTY_PART" : normalized.toUpperCase();
        }
    }

    private static class DecisionMappingInfo {
        String pointIdentifier;
        String pointDescription;
        String choiceIdentifier;
        String choiceDescription;
        DecisionType decisionType;
        String chapterName;
        String questName;

        public DecisionMappingInfo(String pointIdentifier, String pointDescription, String choiceIdentifier, String choiceDescription, DecisionType decisionType, String chapterName, String questName) {
            this.pointIdentifier = pointIdentifier;
            this.pointDescription = pointDescription;
            this.choiceIdentifier = choiceIdentifier;
            this.choiceDescription = choiceDescription;
            this.decisionType = decisionType;
            this.chapterName = chapterName;
            this.questName = questName;
        }
    }


    private void initializeDecisionMapping() {
        logger.info("Initializing decision mapping...");
        decisionMapping = new LinkedHashMap<>();

        DecisionType globalType = decisionTypeRepository.findByName("GLOBAL")
                .orElseThrow(() -> new RuntimeException("GLOBAL type not found during mapping initialization!"));
        DecisionType localType = decisionTypeRepository.findByName("LOCAL")
                .orElseThrow(() -> new RuntimeException("LOCAL type not found during mapping initialization!"));


        // Глобальные решения
        addMapping("Глава 3 — Исток: Выбрать Трисс",
                "W1_G3_ROMANCE", "Выбор романтического интереса",
                "TRISS", "Выбрать Трисс", globalType);

        addMapping("Глава 3 — Исток: Выбрать Шани",
                "W1_G3_ROMANCE", "Выбор романтического интереса",
                "SHANI", "Выбрать Шани", globalType);


        addMapping("Глава 4 — Круги на воде: Компромисс",
                "W1_G4_ARONDIGHT", "Судьба меча Арондит",
                "SWORD_YES", "Компромисс", globalType);

        addMapping("Глава 4 — Круги на воде: Люди/Водяные",
                "W1_G4_ARONDIGHT", "Судьба меча Арондит",
                "SWORD_NO", "Поддержать людей/водяных", globalType);

        addMapping("Глава 4 — Доспехи: Квест выполнен",
                "W1_G4_WOLF_ARMOR", "Получение доспехов Ворона",
                "COMPLETED", "Квест выполнен", globalType);

        addMapping("Глава 4 — Доспехи: Квест не выполнен",
                "W1_G4_WOLF_ARMOR", "Получение доспехов Ворона",
                "NOT_COMPLETED", "Квест не выполнен", globalType);

        addMapping("Глава 4 — Свободные эльфы: Орден",
                "W1_G4_FACTION", "Выбор фракции",
                "ORDER", "Орден", globalType);

        addMapping("Глава 4 — Свободные эльфы: Белки",
                "W1_G4_FACTION", "Выбор фракции",
                "SCOIATAEL", "Белки", globalType);

        addMapping("Глава 4 — Свободные эльфы: Нейтралитет",
                "W1_G4_FACTION", "Выбор фракции",
                "NEUTRALITY", "Нейтралитет", globalType);


        addMapping("Глава 5 — Её Высочество стрыга: Снять проклятие",
                "W1_G5_ADDA_FATE", "Судьба принцессы Адды",
                "LIFT_CURSE", "Снять проклятие", globalType);

        addMapping("Глава 5 — Её Высочество стрыга: Не снимать проклятие",
                "W1_G5_ADDA_FATE", "Судьба принцессы Адды",
                "DONT_LIFT_CURSE", "Не снимать проклятие", globalType);

        // Локальные решения
        addMapping("Пролог — Защита Каэр Морхена: Помочь Трисс",
                "W1_PROLOG_KM_DEFENSE", "Защита Каэр Морхена",
                "HELP_TRISS", "Помочь Трисс", localType);

        addMapping("Пролог — Защита Каэр Морхена: Помочь Весемиру",
                "W1_PROLOG_KM_DEFENSE", "Защита Каэр Морхена",
                "HELP_VESEMIR", "Помочь Весемиру", localType);


        addMapping("Глава 1 — О Людях и чудовищах: Спасти Эбигейл",
                "W1_G1_ABIGAIL", "Судьба Эбигейл",
                "SAVE_ABIGAIL", "Спасти Эбигейл", localType);

        addMapping("Глава 1 — О Людях и чудовищах: Встать на сторону преподобного",
                "W1_G1_ABIGAIL", "Судьба Эбигейл",
                "SIDE_VILLAGERS", "Встать на сторону преподобного", localType);

        addMapping("Глава 1 — Странники в ночи: Отдать оружие белкам",
                "W1_G1_SCOIA_ENCOUNTER", "Встреча с белками",
                "GIVE_WEAPONS", "Отдать оружие белкам", localType);

        addMapping("Глава 1 — Странники в ночи: Убить белок",
                "W1_G1_SCOIA_ENCOUNTER", "Встреча с белками",
                "KILL_SCOIA", "Убить белок", localType);


        addMapping("Глава 2 — Великий побег: Сражаться с Зигфридом",
                "W1_G2_COCKATRICE", "Бой с Кокатриксом",
                "WITH_SIEGFRIED", "Сражаться с Зигфридом", localType);

        addMapping("Глава 2 — Великий побег: Сразиться в одиночку",
                "W1_G2_COCKATRICE", "Бой с Кокатриксом",
                "ALONE", "Сразиться в одиночку", localType);

        addMapping("Глава 2 — Разведка: Помочь Ордену",
                "W1_G2_SWAMP_SCOUTING", "Разведка на болотах",
                "HELP_ORDER", "Помочь Ордену", localType);

        addMapping("Глава 2 — Разведка: Помочь Белкам",
                "W1_G2_SWAMP_SCOUTING", "Разведка на болотах",
                "HELP_SCOIATAEL", "Помочь Белкам", localType);

        addMapping("Глава 2 — Секреты Вызимы: Следовать всем советам Реймонда",
                "W1_G2_RAYMOND_INVEST", "Расследование с Реймондом",
                "FOLLOW_RAYMOND", "Следовать всем советам Реймонда", localType);

        addMapping("Глава 2 — Секреты Вызимы: Расследовать самому",
                "W1_G2_RAYMOND_INVEST", "Расследование с Реймондом",
                "INVESTIGATE_ALONE", "Расследовать самому", localType);


        addMapping("Глава 3 — Замок и Ключ: Вылечить Винсента",
                "W1_G3_VINCENT_FATE", "Судьба Винсента Майса",
                "CURE_VINCENT", "Вылечить Винсента", localType);

        addMapping("Глава 3 — Замок и Ключ: Пощадить без лечения",
                "W1_G3_VINCENT_FATE", "Судьба Винсента Майса",
                "SPARE_WEREWOLF", "Пощадить без лечения", localType);

        addMapping("Глава 3 — Замок и Ключ: Убить",
                "W1_G3_VINCENT_FATE", "Судьба Винсента Майса",
                "KILL_VINCENT", "Убить Винсента", localType);

        addMapping("Глава 3 — Великое ограбление банка: Помочь Белкам",
                "W1_G3_BANK_HEIST", "Ограбление банка Вивальди",
                "HELP_SCOIATAEL_BANK", "Помочь Белкам", localType);

        addMapping("Глава 3 — Великое ограбление банка: Помочь Ордену",
                "W1_G3_BANK_HEIST", "Ограбление банка Вивальди",
                "HELP_ORDER_BANK", "Помочь Ордену", localType);


        addMapping("Глава 4 — Секрет Беренгара: Убить Беренгара",
                "W1_G4_BERENGAR", "Судьба Беренгара",
                "KILL_BERENGAR", "Убить Беренгара", localType);

        addMapping("Глава 4 — Секрет Беренгара: Простить Беренгара",
                "W1_G4_BERENGAR", "Судьба Беренгара",
                "SPARE_BERENGAR", "Простить Беренгара", localType);


        logger.info("Initialized decision mapping for {} unique decisions.", decisionMapping.size());
    }

    private void addMapping(String jsonKey, String pointIdentifier, String pointDescription,
                            String choiceIdentifier, String choiceDescription, DecisionType type) {

        ParsedChoiceData parsed = ParsedChoiceData.parse(jsonKey + " — Dummy");
        if (!parsed.isValid()) {
            logger.error("Failed to parse JSON key for mapping: {}", jsonKey);
            return;
        }

        DecisionMappingInfo info = new DecisionMappingInfo(
                pointIdentifier,
                pointDescription,
                choiceIdentifier,
                choiceDescription,
                type,
                parsed.chapterName,
                parsed.questName
        );
        decisionMapping.put(jsonKey, info);
    }


    // ИНИЦИАЛИЗАЦИЯ ХРОНОЛОГИЧЕСКОГО ПОРЯДКА
    // Тут определяем явный порядок Глав, Квестов и Точек Решений
    private void initializeChronologicalOrder() {
        logger.info("Initializing Witcher 1 chronological order...");
        witcher1ChronologicalOrder = new LinkedHashMap<>(); // Сохраняем порядок Глав

        // Пролог
        LinkedHashMap<String, List<String>> prologueQuests = new LinkedHashMap<>(); // Сохраняем порядок Квестов в Прологе
        prologueQuests.put("Защита Каэр Морхена", List.of("W1_PROLOG_KM_DEFENSE"));
        witcher1ChronologicalOrder.put("Пролог", prologueQuests);

        // Глава 1
        LinkedHashMap<String, List<String>> chapter1Quests = new LinkedHashMap<>();
        chapter1Quests.put("О Людях и чудовищах", List.of("W1_G1_ABIGAIL"));
        chapter1Quests.put("Странники в ночи", List.of("W1_G1_SCOIA_ENCOUNTER"));
        witcher1ChronologicalOrder.put("Глава 1", chapter1Quests);

        // Глава 2
        LinkedHashMap<String, List<String>> chapter2Quests = new LinkedHashMap<>();
        chapter2Quests.put("Великий побег", List.of("W1_G2_COCKATRICE"));
        chapter2Quests.put("Разведка", List.of("W1_G2_SWAMP_SCOUTING"));
        chapter2Quests.put("Секреты Вызимы", List.of("W1_G2_RAYMOND_INVEST"));
        witcher1ChronologicalOrder.put("Глава 2", chapter2Quests);

        // Глава 3
        LinkedHashMap<String, List<String>> chapter3Quests = new LinkedHashMap<>();
        chapter3Quests.put("Замок и Ключ", List.of("W1_G3_VINCENT_FATE"));
        chapter3Quests.put("Великое ограбление банка", List.of("W1_G3_BANK_HEIST"));
        chapter3Quests.put("Исток", List.of("W1_G3_ROMANCE"));

        witcher1ChronologicalOrder.put("Глава 3", chapter3Quests);


        // Глава 4
        LinkedHashMap<String, List<String>> chapter4Quests = new LinkedHashMap<>();
        chapter4Quests.put("Круги на воде", List.of("W1_G4_ARONDIGHT"));
        chapter4Quests.put("Доспехи", List.of("W1_G4_WOLF_ARMOR"));
        chapter4Quests.put("Свободные эльфы", List.of("W1_G4_FACTION"));
        chapter4Quests.put("Секрет Беренгара", List.of("W1_G4_BERENGAR"));
        witcher1ChronologicalOrder.put("Глава 4", chapter4Quests);

        // Глава 5
        LinkedHashMap<String, List<String>> chapter5Quests = new LinkedHashMap<>();
        chapter5Quests.put("Её Высочество стрыга", List.of("W1_G5_ADDA_FATE"));
        witcher1ChronologicalOrder.put("Глава 5", chapter5Quests);

        logger.info("Initialized Witcher 1 chronological order with {} chapters.", witcher1ChronologicalOrder.size());
    }

    // МЕТОД ДЛЯ ЗАГРУЗКИ ДАННЫХ ИЗ JSON И СОЗДАНИЯ СУЩНОСТЕЙ DecisionPoint и DecisionChoice
    // Использует decisionMapping для определения характеристик сущностей.
    private void processWitcher1Data(List<SaveDataJsonEntry> entries, Game game) {
        logger.info("Processing Witcher 1 data entries. Entries count: {}", entries.size());
        Set<String> uniqueChoiceStringsForProcessing = new HashSet<>();
        for (SaveDataJsonEntry entry : entries) {
            for (String choiceString : entry.getChoices()) {
                ParsedChoiceData parsed = ParsedChoiceData.parse(choiceString);
                if (parsed.isValid()) {
                    uniqueChoiceStringsForProcessing.add(parsed.rawPointText);
                }
            }
        }

        logger.info("Found {} unique choice strings to process.", uniqueChoiceStringsForProcessing.size());

        for (String choiceStringKey : uniqueChoiceStringsForProcessing) {
            DecisionMappingInfo mappingInfo = decisionMapping.get(choiceStringKey);

            if (mappingInfo != null) {

                // Находим или создаем DecisionPoint
                DecisionPoint decisionPoint = pointsByIdentifierCache.computeIfAbsent(mappingInfo.pointIdentifier, id -> {
                    logger.info("Creating new DecisionPoint: {} (Type: {})", id, mappingInfo.decisionType.getName());
                    DecisionPoint newPoint = new DecisionPoint();
                    newPoint.setIdentifier(id);
                    newPoint.setGame(game);
                    newPoint.setDecisionType(mappingInfo.decisionType); // Используем тип из маппинга
                    newPoint.setChapterName(mappingInfo.chapterName); // Глава из маппинга
                    newPoint.setQuestName(mappingInfo.questName);   // Квест из маппинга
                    newPoint.setDescription(mappingInfo.pointDescription); // Описание точки из маппинга
                    newPoint.setOrderInGame(null);
                    newPoint.setChoices(new HashSet<>());
                    return decisionPointRepository.save(newPoint); // Сохраняем, чтобы получить ID
                });

                // Находим или создаем DecisionChoice
                String fullChoiceIdentifier = mappingInfo.pointIdentifier + "_" + mappingInfo.choiceIdentifier;
                DecisionChoice decisionChoice = choicesByIdentifierCache.computeIfAbsent(fullChoiceIdentifier, id -> {
                    logger.info("Creating new DecisionChoice: {}", id);
                    DecisionChoice newChoice = new DecisionChoice();
                    newChoice.setIdentifier(mappingInfo.choiceIdentifier); // ID варианта из маппинга
                    newChoice.setDescription(mappingInfo.choiceDescription); // Описание варианта из маппинга

                    // ConsequenceText уникален для каждой комбинации Point+Choice в JSON.
                    // Найдем его из оригинальных записей.
                    // Ищем первую попавшуюся запись, которая соответствует текущему ключу JSON строки.
                    String consequenceText = entries.stream()
                            .flatMap(entry -> entry.getChoices().stream()) // Берем все строки из всех entries
                            .map(ParsedChoiceData::parse) // Парсим каждую строку
                            .filter(p -> p.isValid())
                            .filter(p -> p.rawPointText.equals(choiceStringKey)) // Ищем строку, которая соответствует текущему ключу
                            .findFirst() // Берем первую найденную
                            .map(p -> p.consequenceText) // Извлекаем consequenceText
                            .orElse(""); // Пустая строка, если не найдено


                    newChoice.setConsequenceText(consequenceText);

                    newChoice.setDecisionPoint(decisionPoint); // Связываем с точкой решения
                    decisionPoint.getChoices().add(newChoice); // Добавляем выбор в коллекцию точки решения

                    return decisionChoiceRepository.save(newChoice); // Сохраняем выбор
                });

                // Обновляем consequenceText
                // Снова ищем consequenceText как выше
                String currentConsequenceText = entries.stream()
                        .flatMap(entry -> entry.getChoices().stream())
                        .map(ParsedChoiceData::parse)
                        .filter(p -> p.isValid())
                        .filter(p -> p.rawPointText.equals(choiceStringKey))
                        .findFirst()
                        .map(p -> p.consequenceText)
                        .orElse("");

                if (!decisionChoice.getConsequenceText().equals(currentConsequenceText)) {
                    logger.debug("Updating consequence text for choice {}. From '{}' to '{}'",
                            decisionChoice.getIdentifier(), decisionChoice.getConsequenceText(), currentConsequenceText);
                    decisionChoice.setConsequenceText(currentConsequenceText);
                    decisionChoiceRepository.save(decisionChoice);
                }

            } else {
                logger.warn("No decision mapping found for key: '{}' from JSON. Skipping entity creation.", choiceStringKey);
            }
        }
        logger.info("Finished entity creation for processed JSON entries. Points cache size: {}, Choices cache size: {}",
                pointsByIdentifierCache.size(), choicesByIdentifierCache.size());
    }


    // МЕТОД: ПРИСВАИВАЕТ orderInGame НА ОСНОВЕ ХРОНОЛОГИЧЕСКОГО ПОРЯДКА
    private void assignChronologicalOrderToPoints() {
        logger.info("Assigning chronological order to DecisionPoints...");

        int currentOrder = 1; // Начинаем общий счетчик порядка

        // Итерируем по предопределенному хронологическому порядку глав
        for (Map.Entry<String, LinkedHashMap<String, List<String>>> chapterEntry : witcher1ChronologicalOrder.entrySet()) {
            String chapterName = chapterEntry.getKey();
            LinkedHashMap<String, List<String>> questsInChapter = chapterEntry.getValue();

            // Итерируем по квестам в предопределенном порядке
            for (Map.Entry<String, List<String>> questEntry : questsInChapter.entrySet()) {
                String questName = questEntry.getKey();
                List<String> pointIdentifiersInQuest = questEntry.getValue();

                // Итерируем по точкам решений внутри квеста в предопределенном порядке
                for (String pointIdentifier : pointIdentifiersInQuest) {
                    DecisionPoint decisionPoint = pointsByIdentifierCache.get(pointIdentifier);

                    if (decisionPoint != null) {
                        // Присваиваем orderInGame и сохраняем
                        decisionPoint.setOrderInGame(currentOrder);
                        decisionPointRepository.save(decisionPoint); // Сохраняем обновленный orderInGame
                        currentOrder++;
                    } else {
                        logger.warn("DecisionPoint with identifier {} defined in chronological order was NOT found in cache/DB. Check JSON data and mapping.", pointIdentifier);
                    }
                }
            }
        }
        logger.info("Finished assigning chronological order. Total order assigned: {}", currentOrder - 1);

        pointsByIdentifierCache.values().stream()
                .filter(p -> p.getOrderInGame() == null)
                .forEach(p -> logger.warn("DecisionPoint {} (Chapter: {}, Quest: {}) did NOT have order assigned. Missing from chronological order mapping?",
                        p.getIdentifier(), p.getChapterName(), p.getQuestName()));
    }

    // МЕТОД ДЛЯ СОЗДАНИЯ ГЛОБАЛЬНЫХ КОНФИГУРАЦИЙ СОХРАНЕНИЙ И ФАЙЛОВ PREMADESAVE
    // Итерирует ТОЛЬКО по глобальным записям JSON и использует кэшированные сущности.
    private void processWitcher1GlobalConfigs(List<SaveDataJsonEntry> globalSaveEntries, Game w1Game, Game w2Game) {
        logger.info("Processing global save configurations and premade saves...");

        for (SaveDataJsonEntry entry : globalSaveEntries) {
            String originalSaveName = entry.getSave_name();
            String actualFilename = globalSaveNameToFileMap.get(originalSaveName);

            if (actualFilename == null) {
                logger.warn("No filename mapping for global save entry name: {}. Skipping config creation.", originalSaveName);
                continue;
            }

            StringBuilder configNameBuilder = new StringBuilder("W1_CONFIG");
            Map<String, String> currentConfigChoicesMap = new HashMap<>();

            // Собираем стандартизированные идентификаторы ГЛОБАЛЬНЫХ решений из текущего entry
            for (String choiceString : entry.getChoices()) {
                ParsedChoiceData parsed = ParsedChoiceData.parse(choiceString);
                if (!parsed.isValid()) continue;

                String choiceStringKey = parsed.rawPointText; // Ключ для поиска в маппинге

                // Находим информацию о маппинге для этой строки
                DecisionMappingInfo mappingInfo = decisionMapping.get(choiceStringKey);

                // Проверяем, что маппинг существует И тип решения ГЛОБАЛЬНЫЙ
                if (mappingInfo != null && "GLOBAL".equals(mappingInfo.decisionType.getName())) {
                    currentConfigChoicesMap.put(mappingInfo.pointIdentifier, mappingInfo.choiceIdentifier);
                } else if (mappingInfo != null) {

                    logger.debug("Skipping non-GLOBAL decision ({}) for global config entry: {}", mappingInfo.decisionType.getName(), choiceStringKey);
                } else {

                    logger.warn("No decision mapping found for global config entry key: '{}'. Skipping.", choiceStringKey);
                }
            }

            if (currentConfigChoicesMap.isEmpty()) {
                logger.warn("No mapped GLOBAL decisions found for config entry: {}. Skipping SaveConfiguration/PremadeSave creation.", originalSaveName);
                continue;
            }

            // Генерируем каноническое имя конфигурации на основе найденных глобальных решений
            List<String> sortedPointIdentifiers = currentConfigChoicesMap.keySet().stream().sorted().collect(Collectors.toList());
            for (String pointId : sortedPointIdentifiers) {
                configNameBuilder.append("_").append(pointId).append("_").append(currentConfigChoicesMap.get(pointId));
            }
            String canonicalConfigName = configNameBuilder.toString();

            // Находим или создаем SaveConfiguration
            SaveConfiguration configuration = saveConfigurationRepository.findByName(canonicalConfigName).orElseGet(() -> {
                logger.info("Creating new SaveConfiguration: {}", canonicalConfigName);
                SaveConfiguration newConfig = new SaveConfiguration();
                newConfig.setName(canonicalConfigName);
                newConfig.setDescription("Configuration derived from: " + originalSaveName); // Описание из оригинального save_name
                newConfig.setOriginatingGame(w1Game); // Связываем с Ведьмаком 1
                return saveConfigurationRepository.save(newConfig);
            });

            // Создаем ConfigurationChoice для каждого решения в этой конфигурации
            for (Map.Entry<String, String> configChoiceEntry : currentConfigChoicesMap.entrySet()) {
                String pointId = configChoiceEntry.getKey();
                String choiceId = configChoiceEntry.getValue();

                // Находим сущности DecisionPoint и DecisionChoice из кэша по их стандартизированным ID
                DecisionPoint decisionPoint = pointsByIdentifierCache.get(pointId);
                String fullChoiceIdentifier = pointId + "_" + choiceId;
                DecisionChoice decisionChoice = choicesByIdentifierCache.get(fullChoiceIdentifier);

                if (decisionPoint != null && decisionChoice != null) {
                    if ("GLOBAL".equals(decisionPoint.getDecisionType().getName())) {
                        boolean exists = configurationChoiceRepository.existsByConfigurationAndDecisionPoint(configuration, decisionPoint);
                        if (!exists) {
                            logger.debug("Adding ConfigurationChoice for config {} and point {}", configuration.getName(), decisionPoint.getIdentifier());
                            ConfigurationChoice configChoice = new ConfigurationChoice(configuration, decisionPoint, decisionChoice);
                            configurationChoiceRepository.save(configChoice);
                        } else {
                            logger.debug("ConfigurationChoice already exists for config {} and point {}", configuration.getName(), decisionPoint.getIdentifier());
                        }
                    } else {
                        logger.error("Attempted to add non-GLOBAL decision point {} to a global configuration {}. This should not happen based on filtering.", pointId, configuration.getName());
                    }
                } else {
                    logger.error("CONFIG_ERROR: Could not find cached GLOBAL DecisionPoint ({}) or DecisionChoice ({}) for config {}. This should not happen.", pointId, fullChoiceIdentifier, configuration.getName());
                }
            }

            // Находим игру, для которой предназначено сохранение (Ведьмак 2)
            Game w2GameTarget = gameRepository.findByName("The Witcher 2: Assassins of Kings")
                    .orElseThrow(() -> new RuntimeException("The Witcher 2 not found in DB! Cannot link W1 saves."));

            // Создаем PremadeSave для этого файла, связанный с конфигурацией и целевой игрой
            boolean saveExists = premadeSaveRepository.existsByConfigurationAndGame(configuration, w2GameTarget);
            if (!saveExists) {
                logger.info("Creating new PremadeSave for config {} (filename: {})", configuration.getName(), actualFilename);
                PremadeSave premadeSave = new PremadeSave();
                premadeSave.setConfiguration(configuration); // Связываем с созданной конфигурацией
                premadeSave.setGame(w2GameTarget); // Указываем, что это сохранение для Ведьмака 2
                premadeSave.setFilename(actualFilename); // Используем реальное имя файла
                premadeSave.setDescription("W1 Save Config: " + originalSaveName); // Описание файла из оригинального save_name
                premadeSaveRepository.save(premadeSave);
            } else {
                premadeSaveRepository.findByConfigurationAndGame(configuration, w2GameTarget).ifPresent(save -> {
                    if (!save.getFilename().equals(actualFilename)) {
                        logger.info("Updating filename for existing PremadeSave (Config: {}) from {} to {}", configuration.getName(), save.getFilename(), actualFilename);
                        save.setFilename(actualFilename);
                        premadeSaveRepository.save(save);
                    }
                });
            }
        }
        logger.info("Finished processing global save configurations.");
    }


    // Метод для инициализации карты сопоставления save_name из глобальных JSON с реальными именами файлов
    private void initializeFilenameMapping() {
        globalSaveNameToFileMap = new LinkedHashMap<>(); // Для сохранения порядка

        globalSaveNameToFileMap.put("Сохранение 1+п(574) (Орден, Трисс, Доспехи да, Арондит да, Д'Иабль да)", "000574 - Орден Пылающей Розы-130.TheWitcherSave");
        globalSaveNameToFileMap.put("Сохранение 2+п(1014) (Орден, Трисс, Доспехи да, Арондит да, Д'Иабль нет)", "001014 - Орден Пылающей Розы-1014.TheWitcherSave");
        globalSaveNameToFileMap.put("Сохранение 3+п(1015) (Орден, Трисс, Доспехи да, Арондит нет, Д'Иабль да)", "001015 - Орден Пылающей Розы-1015.TheWitcherSave");
        globalSaveNameToFileMap.put("Сохранение 4+п(1016) (Орден, Трисс, Доспехи да, Арондит нет, Д'Иабль нет)", "001016 - Орден Пылающей Розы-1016.TheWitcherSave");
        globalSaveNameToFileMap.put("Сохранение 5+п(1017) (Орден, Трисс, Доспехи нет, Арондит да, Д'Иабль да)", "001017 - Орден Пылающей Розы-053.TheWitcherSave");
        globalSaveNameToFileMap.put("Сохранение 6+п(1018) (Орден, Трисс, Доспехи нет, Арондит да, Д'Иабль нет)", "001018 - Орден Пылающей Розы-054.TheWitcherSave");
        globalSaveNameToFileMap.put("Сохранение 7+п(1019) (Орден, Трисс, Доспехи нет, Арондит нет, Д'Иабль да)", "001019 - Орден Пылающей Розы-055.TheWitcherSave");
        globalSaveNameToFileMap.put("Сохранение 8+п(1020) (Орден, Трисс, Доспехи нет, Арондит нет, Д'Иабль нет)", "001020 - Орден Пылающей Розы-056.TheWitcherSave");
        globalSaveNameToFileMap.put("Сохранение 9+п(1021) (Орден, Шани, Доспехи да, Арондит да, Д'Иабль да)", "001021 - Орден Пылающей Розы-057.TheWitcherSave");
        globalSaveNameToFileMap.put("Сохранение 10+п(1022) (Орден, Шани, Доспехи да, Арондит да, Д'Иабль нет)", "001022 - Орден Пылающей Розы-058.TheWitcherSave");
        globalSaveNameToFileMap.put("Сохранение 11+п(1023) (Орден, Шани, Доспехи да, Арондит нет, Д'Иабль да)", "001023 - Орден Пылающей Розы-059.TheWitcherSave");
        globalSaveNameToFileMap.put("Сохранение 12+п(1024) (Орден, Шани, Доспехи да, Арондит нет, Д'Иабль нет)", "001024 - Орден Пылающей Розы-060.TheWitcherSave");
        globalSaveNameToFileMap.put("Сохранение 13+п(1025) (Орден, Шани, Доспехи нет, Арондит да, Д'Иабль да)", "001025 - Орден Пылающей Розы-061.TheWitcherSave");
        globalSaveNameToFileMap.put("Сохранение 14+п(1026) (Орден, Шани, Доспехи нет, Арондит да, Д'Иабль нет)", "001026 - Орден Пылающей Розы-062.TheWitcherSave");
        globalSaveNameToFileMap.put("Сохранение 15+п(1027) (Орден, Шани, Доспехи нет, Арондит нет, Д'Иабль да)", "001027 - Орден Пылающей Розы-063.TheWitcherSave");
        globalSaveNameToFileMap.put("Сохранение 16+п(1028) (Орден, Шани, Доспехи нет, Арондит нет, Д'Иабль нет)", "001028 - Орден Пылающей Розы-064.TheWitcherSave");

        // Позже нужно будет добавить сохранения для белок и нейтралитета.

        logger.info("Initialized filename mapping for {} global save entries.", globalSaveNameToFileMap.size());
    }

    // Загрузка основных сущностей: Игры, Типы решений
    private void loadBaseData() {
        logger.info("Loading base data (Games, Decision Types)...");
        gameRepository.findByName("The Witcher").orElseGet(() -> gameRepository.save(new Game("The Witcher", 2007)));
        gameRepository.findByName("The Witcher 2: Assassins of Kings").orElseGet(() -> gameRepository.save(new Game("The Witcher 2: Assassins of Kings", 2011)));
        gameRepository.findByName("The Witcher 3: Wild Hunt").orElseGet(() -> gameRepository.save(new Game("The Witcher 3: Wild Hunt", 2015)));
        decisionTypeRepository.findByName("GLOBAL").orElseGet(() -> decisionTypeRepository.save(new DecisionType("GLOBAL", "Решение, которое может иметь влияние на последующие игры или финальный ролик")));
        decisionTypeRepository.findByName("LOCAL").orElseGet(() -> decisionTypeRepository.save(new DecisionType("LOCAL", "Решение, влияющее только на текущее прохождение")));
        logger.info("Base data loaded.");
    }
}