-- MySQL dump 10.13  Distrib 8.0.38, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: witcher_db
-- ------------------------------------------------------
-- Server version	8.0.39

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `configuration_choices`
--

DROP TABLE IF EXISTS `configuration_choices`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `configuration_choices` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `configuration_id` bigint NOT NULL COMMENT 'ID конфигурации мира',
  `decision_point_id` bigint NOT NULL COMMENT 'ID точки решения',
  `chosen_choice_id` bigint NOT NULL COMMENT 'ID выбранного варианта выбора для этой точки в этой конфигурации',
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_choice_in_config` (`configuration_id`,`decision_point_id`),
  KEY `decision_point_id` (`decision_point_id`),
  KEY `chosen_choice_id` (`chosen_choice_id`),
  CONSTRAINT `configuration_choices_ibfk_1` FOREIGN KEY (`configuration_id`) REFERENCES `save_configurations` (`id`) ON DELETE CASCADE,
  CONSTRAINT `configuration_choices_ibfk_2` FOREIGN KEY (`decision_point_id`) REFERENCES `decision_points` (`id`) ON DELETE CASCADE,
  CONSTRAINT `configuration_choices_ibfk_3` FOREIGN KEY (`chosen_choice_id`) REFERENCES `decision_choices` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=561 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Выборы, определенные для готовых конфигураций мира';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `configuration_choices`
--

LOCK TABLES `configuration_choices` WRITE;
/*!40000 ALTER TABLE `configuration_choices` DISABLE KEYS */;
INSERT INTO `configuration_choices` VALUES (481,97,86,180),(482,97,87,188),(483,97,85,179),(484,97,88,183),(485,97,89,189),(486,98,86,180),(487,98,87,188),(488,98,85,179),(489,98,88,183),(490,98,89,185),(491,99,86,182),(492,99,87,188),(493,99,85,179),(494,99,88,183),(495,99,89,189),(496,100,86,182),(497,100,87,188),(498,100,85,179),(499,100,88,183),(500,100,89,185),(501,101,86,180),(502,101,87,181),(503,101,85,179),(504,101,88,183),(505,101,89,189),(506,102,86,180),(507,102,87,181),(508,102,85,179),(509,102,88,183),(510,102,89,185),(511,103,86,182),(512,103,87,181),(513,103,85,179),(514,103,88,183),(515,103,89,189),(516,104,86,182),(517,104,87,181),(518,104,85,179),(519,104,88,183),(520,104,89,185),(521,105,86,180),(522,105,87,188),(523,105,85,187),(524,105,88,183),(525,105,89,189),(526,106,86,180),(527,106,87,188),(528,106,85,187),(529,106,88,183),(530,106,89,185),(531,107,86,182),(532,107,87,188),(533,107,85,187),(534,107,88,183),(535,107,89,189),(536,108,86,182),(537,108,87,188),(538,108,85,187),(539,108,88,183),(540,108,89,185),(541,109,86,180),(542,109,87,181),(543,109,85,187),(544,109,88,183),(545,109,89,189),(546,110,86,180),(547,110,87,181),(548,110,85,187),(549,110,88,183),(550,110,89,185),(551,111,86,182),(552,111,87,181),(553,111,85,187),(554,111,88,183),(555,111,89,189),(556,112,86,182),(557,112,87,181),(558,112,85,187),(559,112,88,183),(560,112,89,185);
/*!40000 ALTER TABLE `configuration_choices` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `decision_choices`
--

DROP TABLE IF EXISTS `decision_choices`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `decision_choices` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `decision_point_id` bigint NOT NULL COMMENT 'ID точки решения, к которой относится вариант',
  `identifier` varchar(255) NOT NULL COMMENT 'Уникальный идентификатор варианта выбора в рамках точки решения',
  `description` text COMMENT 'Описание самого варианта выбора',
  `consequence_text` text COMMENT 'Краткое описание непосредственных последствий выбора',
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_choice_in_point` (`decision_point_id`,`identifier`),
  CONSTRAINT `decision_choices_ibfk_1` FOREIGN KEY (`decision_point_id`) REFERENCES `decision_points` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=209 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Варианты выбора для решений';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `decision_choices`
--

LOCK TABLES `decision_choices` WRITE;
/*!40000 ALTER TABLE `decision_choices` DISABLE KEYS */;
INSERT INTO `decision_choices` VALUES (179,85,'TRISS','Выбрать Трисс','В The Witcher 2 ничего не изменится.'),(180,86,'SWORD_YES','Компромисс','Меч Арондит перенесется.'),(181,87,'NOT_COMPLETED','Квест не выполнен','Доспехи не будут импортированы.'),(182,86,'SWORD_NO','Поддержать людей/водяных','Меч Арондит не перенесется.'),(183,88,'ORDER','Орден','Появится Зигфрид.'),(184,88,'SCOIATAEL','Белки','Упоминание Яевинна.'),(185,89,'DONT_LIFT_CURSE','Не снимать проклятие','ничего не изменится.'),(186,88,'NEUTRALITY','Нейтралитет','Упоминание Яевинна.'),(187,85,'SHANI','Выбрать Шани','Будет запись в дневнике.'),(188,87,'COMPLETED','Квест выполнен','Доспехи будут импортированы.'),(189,89,'LIFT_CURSE','Снять проклятие','Меч Д\'Иабль и упоминание Адды.'),(190,90,'SAVE_ABIGAIL','Спасти Эбигейл','Эбигейл поможет в боях в Главе IV и эпилоге.'),(191,91,'SPARE_WEREWOLF','Пощадить без лечения','Винсент сражается как оборотень.'),(192,92,'GIVE_WEAPONS','Отдать оружие белкам','Белки убьют Кирпича в Главе II, кузнец торгует в 2 главе.'),(193,93,'HELP_ORDER_BANK','Помочь Ордену','Помогут в битве. Влияет на доступные пути в Главе IV.'),(194,94,'HELP_SCOIATAEL','Помочь Белкам','200 оренов, информация о Беренгаре.'),(195,95,'INVESTIGATE_ALONE','Расследовать самому','Щит будет защищать Геральта в битве с профессором.'),(196,95,'FOLLOW_RAYMOND','Следовать всем советам Реймонда','Щит Альзура будет защищать профессора в битве с Геральтом.'),(197,96,'SPARE_BERENGAR','Простить Беренгара','Только сок и книга, но Беренгар поможет в битве.'),(198,96,'KILL_BERENGAR','Убить Беренгара','Медальон, сок Дагона, книга.'),(199,97,'ALONE','Сразиться в одиночку','Легче попасть в больницу, но оружейник Ордена не поможет.'),(200,91,'CURE_VINCENT','Вылечить Винсента','Винсент поможет в битве.'),(201,93,'HELP_SCOIATAEL_BANK','Помочь Белкам','Помогут в битве с Азаром Джаведом.'),(202,92,'KILL_SCOIA','Убить белок','Голан Вивальди окажется в тюрьме (можно выкупить за 200 оренов). Убийство белок даёт доступ к информации о Хуге.'),(203,98,'HELP_VESEMIR','Помочь Весемиру','Весемир даст красную метеоритную руду. В Главе I на мосту будет саламандра и собака-мутант, кат-сцена негативная.'),(204,94,'HELP_ORDER','Помочь Ордену','200 оренов, информация о Беренгаре.'),(205,91,'KILL_VINCENT','Убить Винсента','получите мех и зелье.'),(206,98,'HELP_TRISS','Помочь Трисс','Можно забрать сапфир с трупа Саволлы. На мосту только саламандра, кат-сцена положительная.'),(207,97,'WITH_SIEGFRIED','Сражаться с Зигфридом','Легче попасть на дамбу, оружейник Ордена торгует.'),(208,90,'SIDE_VILLAGERS','Встать на сторону преподобного','В Главе IV в хижине будет обычный целитель, в эпилоге сразитесь с Эбигейл.');
/*!40000 ALTER TABLE `decision_choices` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `decision_points`
--

DROP TABLE IF EXISTS `decision_points`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `decision_points` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `game_id` bigint NOT NULL COMMENT 'ID игры, к которой относится точка решения',
  `chapter_name` varchar(255) DEFAULT NULL COMMENT 'Название главы, к которой относится точка решения',
  `decision_type_id` bigint NOT NULL COMMENT 'ID типа решения (глобальное, локальное и т.д.)',
  `identifier` varchar(255) NOT NULL COMMENT 'Уникальный идентификатор точки решения в рамках игры',
  `description` text COMMENT 'Описание ситуации, где принимается решение',
  `quest_name` varchar(255) DEFAULT NULL COMMENT 'Название квеста, если решение принимается в квесте',
  `order_in_game` int DEFAULT NULL COMMENT 'Порядковый номер или приблизительное местоположение в игре для сортировки',
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_decision_in_game` (`game_id`,`identifier`),
  KEY `decision_type_id` (`decision_type_id`),
  CONSTRAINT `decision_points_ibfk_1` FOREIGN KEY (`game_id`) REFERENCES `games` (`id`) ON DELETE CASCADE,
  CONSTRAINT `decision_points_ibfk_2` FOREIGN KEY (`decision_type_id`) REFERENCES `decision_types` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=99 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Точки принятия решений в играх';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `decision_points`
--

LOCK TABLES `decision_points` WRITE;
/*!40000 ALTER TABLE `decision_points` DISABLE KEYS */;
INSERT INTO `decision_points` VALUES (85,1,'Глава 3',1,'W1_G3_ROMANCE','Выбор романтического интереса','Исток',9),(86,1,'Глава 4',1,'W1_G4_ARONDIGHT','Судьба меча Арондит','Круги на воде',10),(87,1,'Глава 4',1,'W1_G4_WOLF_ARMOR','Получение доспехов Ворона','Доспехи',11),(88,1,'Глава 4',1,'W1_G4_FACTION','Выбор фракции','Свободные эльфы',12),(89,1,'Глава 5',1,'W1_G5_ADDA_FATE','Судьба принцессы Адды','Её Высочество стрыга',14),(90,1,'Глава 1',2,'W1_G1_ABIGAIL','Судьба Эбигейл','О Людях и чудовищах',2),(91,1,'Глава 3',2,'W1_G3_VINCENT_FATE','Судьба Винсента Майса','Замок и Ключ',7),(92,1,'Глава 1',2,'W1_G1_SCOIA_ENCOUNTER','Встреча с белками','Странники в ночи',3),(93,1,'Глава 3',2,'W1_G3_BANK_HEIST','Ограбление банка Вивальди','Великое ограбление банка',8),(94,1,'Глава 2',2,'W1_G2_SWAMP_SCOUTING','Разведка на болотах','Разведка',5),(95,1,'Глава 2',2,'W1_G2_RAYMOND_INVEST','Расследование с Реймондом','Секреты Вызимы',6),(96,1,'Глава 4',2,'W1_G4_BERENGAR','Судьба Беренгара','Секрет Беренгара',13),(97,1,'Глава 2',2,'W1_G2_COCKATRICE','Бой с Кокатриксом','Великий побег',4),(98,1,'Пролог',2,'W1_PROLOG_KM_DEFENSE','Защита Каэр Морхена','Защита Каэр Морхена',1);
/*!40000 ALTER TABLE `decision_points` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `decision_types`
--

DROP TABLE IF EXISTS `decision_types`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `decision_types` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL COMMENT 'Название типа решения (напр. GLOBAL, LOCAL)',
  `description` varchar(255) DEFAULT NULL COMMENT 'Краткое описание типа решения',
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Таблица типов решений';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `decision_types`
--

LOCK TABLES `decision_types` WRITE;
/*!40000 ALTER TABLE `decision_types` DISABLE KEYS */;
INSERT INTO `decision_types` VALUES (1,'GLOBAL','Решение, которое может иметь влияние на последующие игры'),(2,'LOCAL','Решение, влияющее только на текущее прохождение, не переносится напрямую в другие игры');
/*!40000 ALTER TABLE `decision_types` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `games`
--

DROP TABLE IF EXISTS `games`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `games` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL COMMENT 'Название игры (напр. The Witcher, The Witcher 2, The Witcher 3)',
  `release_year` int DEFAULT NULL COMMENT 'Год выпуска игры',
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Таблица игр серии Ведьмак';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `games`
--

LOCK TABLES `games` WRITE;
/*!40000 ALTER TABLE `games` DISABLE KEYS */;
INSERT INTO `games` VALUES (1,'The Witcher',2007),(2,'The Witcher 2: Assassins of Kings',2011),(3,'The Witcher 3: Wild Hunt',2015);
/*!40000 ALTER TABLE `games` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `premade_saves`
--

DROP TABLE IF EXISTS `premade_saves`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `premade_saves` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `configuration_id` bigint NOT NULL COMMENT 'ID конфигурации мира, которую представляет этот файл сохранения',
  `game_id` bigint NOT NULL COMMENT 'ID игры, ДЛЯ КОТОРОЙ предназначен этот файл сохранения (напр. W2, W3)',
  `filename` varchar(255) NOT NULL COMMENT 'Имя файла сохранения на сервере (без полного пути)',
  `description` text COMMENT 'Описание файла сохранения',
  `upload_date` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Дата загрузки файла',
  PRIMARY KEY (`id`),
  KEY `configuration_id` (`configuration_id`),
  KEY `game_id` (`game_id`),
  CONSTRAINT `premade_saves_ibfk_1` FOREIGN KEY (`configuration_id`) REFERENCES `save_configurations` (`id`) ON DELETE CASCADE,
  CONSTRAINT `premade_saves_ibfk_2` FOREIGN KEY (`game_id`) REFERENCES `games` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=113 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Готовые файлы сохранений для скачивания, соответствующие конфигурациям';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `premade_saves`
--

LOCK TABLES `premade_saves` WRITE;
/*!40000 ALTER TABLE `premade_saves` DISABLE KEYS */;
INSERT INTO `premade_saves` VALUES (97,97,2,'000574 - Орден Пылающей Розы-130.TheWitcherSave','W1 Save Config: Сохранение 1+п(574) (Орден, Трисс, Доспехи да, Арондит да, Д\'Иабль да)','2025-05-29 03:19:24'),(98,98,2,'001014 - Орден Пылающей Розы-1014.TheWitcherSave','W1 Save Config: Сохранение 2+п(1014) (Орден, Трисс, Доспехи да, Арондит да, Д\'Иабль нет)','2025-05-29 03:19:24'),(99,99,2,'001015 - Орден Пылающей Розы-1015.TheWitcherSave','W1 Save Config: Сохранение 3+п(1015) (Орден, Трисс, Доспехи да, Арондит нет, Д\'Иабль да)','2025-05-29 03:19:24'),(100,100,2,'001016 - Орден Пылающей Розы-1016.TheWitcherSave','W1 Save Config: Сохранение 4+п(1016) (Орден, Трисс, Доспехи да, Арондит нет, Д\'Иабль нет)','2025-05-29 03:19:24'),(101,101,2,'001017 - Орден Пылающей Розы-053.TheWitcherSave','W1 Save Config: Сохранение 5+п(1017) (Орден, Трисс, Доспехи нет, Арондит да, Д\'Иабль да)','2025-05-29 03:19:24'),(102,102,2,'001018 - Орден Пылающей Розы-054.TheWitcherSave','W1 Save Config: Сохранение 6+п(1018) (Орден, Трисс, Доспехи нет, Арондит да, Д\'Иабль нет)','2025-05-29 03:19:24'),(103,103,2,'001019 - Орден Пылающей Розы-055.TheWitcherSave','W1 Save Config: Сохранение 7+п(1019) (Орден, Трисс, Доспехи нет, Арондит нет, Д\'Иабль да)','2025-05-29 03:19:24'),(104,104,2,'001020 - Орден Пылающей Розы-056.TheWitcherSave','W1 Save Config: Сохранение 8+п(1020) (Орден, Трисс, Доспехи нет, Арондит нет, Д\'Иабль нет)','2025-05-29 03:19:24'),(105,105,2,'001021 - Орден Пылающей Розы-057.TheWitcherSave','W1 Save Config: Сохранение 9+п(1021) (Орден, Шани, Доспехи да, Арондит да, Д\'Иабль да)','2025-05-29 03:19:24'),(106,106,2,'001022 - Орден Пылающей Розы-058.TheWitcherSave','W1 Save Config: Сохранение 10+п(1022) (Орден, Шани, Доспехи да, Арондит да, Д\'Иабль нет)','2025-05-29 03:19:24'),(107,107,2,'001023 - Орден Пылающей Розы-059.TheWitcherSave','W1 Save Config: Сохранение 11+п(1023) (Орден, Шани, Доспехи да, Арондит нет, Д\'Иабль да)','2025-05-29 03:19:24'),(108,108,2,'001024 - Орден Пылающей Розы-060.TheWitcherSave','W1 Save Config: Сохранение 12+п(1024) (Орден, Шани, Доспехи да, Арондит нет, Д\'Иабль нет)','2025-05-29 03:19:24'),(109,109,2,'001025 - Орден Пылающей Розы-061.TheWitcherSave','W1 Save Config: Сохранение 13+п(1025) (Орден, Шани, Доспехи нет, Арондит да, Д\'Иабль да)','2025-05-29 03:19:24'),(110,110,2,'001026 - Орден Пылающей Розы-062.TheWitcherSave','W1 Save Config: Сохранение 14+п(1026) (Орден, Шани, Доспехи нет, Арондит да, Д\'Иабль нет)','2025-05-29 03:19:24'),(111,111,2,'001027 - Орден Пылающей Розы-063.TheWitcherSave','W1 Save Config: Сохранение 15+п(1027) (Орден, Шани, Доспехи нет, Арондит нет, Д\'Иабль да)','2025-05-29 03:19:24'),(112,112,2,'001028 - Орден Пылающей Розы-064.TheWitcherSave','W1 Save Config: Сохранение 16+п(1028) (Орден, Шани, Доспехи нет, Арондит нет, Д\'Иабль нет)','2025-05-29 03:19:24');
/*!40000 ALTER TABLE `premade_saves` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `roles`
--

DROP TABLE IF EXISTS `roles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `roles` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(20) NOT NULL COMMENT 'Название роли (напр. ROLE_USER, ROLE_ADMIN)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Таблица ролей пользователей';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `roles`
--

LOCK TABLES `roles` WRITE;
/*!40000 ALTER TABLE `roles` DISABLE KEYS */;
INSERT INTO `roles` VALUES (2,'ROLE_ADMIN'),(1,'ROLE_USER');
/*!40000 ALTER TABLE `roles` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `save_configurations`
--

DROP TABLE IF EXISTS `save_configurations`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `save_configurations` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL COMMENT 'Название конфигурации',
  `description` text COMMENT 'Описание этой конфигурации мира',
  `originating_game_id` bigint NOT NULL COMMENT 'ID игры, решения которой описывает эта конфигурация (напр. W1)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`),
  KEY `originating_game_id` (`originating_game_id`),
  CONSTRAINT `save_configurations_ibfk_1` FOREIGN KEY (`originating_game_id`) REFERENCES `games` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=113 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Готовые конфигурации мира, описывающие наборы решений';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `save_configurations`
--

LOCK TABLES `save_configurations` WRITE;
/*!40000 ALTER TABLE `save_configurations` DISABLE KEYS */;
INSERT INTO `save_configurations` VALUES (97,'W1_CONFIG_W1_G3_ROMANCE_TRISS_W1_G4_ARONDIGHT_SWORD_YES_W1_G4_FACTION_ORDER_W1_G4_WOLF_ARMOR_COMPLETED_W1_G5_ADDA_FATE_LIFT_CURSE','Configuration derived from: Сохранение 1+п(574) (Орден, Трисс, Доспехи да, Арондит да, Д\'Иабль да)',1),(98,'W1_CONFIG_W1_G3_ROMANCE_TRISS_W1_G4_ARONDIGHT_SWORD_YES_W1_G4_FACTION_ORDER_W1_G4_WOLF_ARMOR_COMPLETED_W1_G5_ADDA_FATE_DONT_LIFT_CURSE','Configuration derived from: Сохранение 2+п(1014) (Орден, Трисс, Доспехи да, Арондит да, Д\'Иабль нет)',1),(99,'W1_CONFIG_W1_G3_ROMANCE_TRISS_W1_G4_ARONDIGHT_SWORD_NO_W1_G4_FACTION_ORDER_W1_G4_WOLF_ARMOR_COMPLETED_W1_G5_ADDA_FATE_LIFT_CURSE','Configuration derived from: Сохранение 3+п(1015) (Орден, Трисс, Доспехи да, Арондит нет, Д\'Иабль да)',1),(100,'W1_CONFIG_W1_G3_ROMANCE_TRISS_W1_G4_ARONDIGHT_SWORD_NO_W1_G4_FACTION_ORDER_W1_G4_WOLF_ARMOR_COMPLETED_W1_G5_ADDA_FATE_DONT_LIFT_CURSE','Configuration derived from: Сохранение 4+п(1016) (Орден, Трисс, Доспехи да, Арондит нет, Д\'Иабль нет)',1),(101,'W1_CONFIG_W1_G3_ROMANCE_TRISS_W1_G4_ARONDIGHT_SWORD_YES_W1_G4_FACTION_ORDER_W1_G4_WOLF_ARMOR_NOT_COMPLETED_W1_G5_ADDA_FATE_LIFT_CURSE','Configuration derived from: Сохранение 5+п(1017) (Орден, Трисс, Доспехи нет, Арондит да, Д\'Иабль да)',1),(102,'W1_CONFIG_W1_G3_ROMANCE_TRISS_W1_G4_ARONDIGHT_SWORD_YES_W1_G4_FACTION_ORDER_W1_G4_WOLF_ARMOR_NOT_COMPLETED_W1_G5_ADDA_FATE_DONT_LIFT_CURSE','Configuration derived from: Сохранение 6+п(1018) (Орден, Трисс, Доспехи нет, Арондит да, Д\'Иабль нет)',1),(103,'W1_CONFIG_W1_G3_ROMANCE_TRISS_W1_G4_ARONDIGHT_SWORD_NO_W1_G4_FACTION_ORDER_W1_G4_WOLF_ARMOR_NOT_COMPLETED_W1_G5_ADDA_FATE_LIFT_CURSE','Configuration derived from: Сохранение 7+п(1019) (Орден, Трисс, Доспехи нет, Арондит нет, Д\'Иабль да)',1),(104,'W1_CONFIG_W1_G3_ROMANCE_TRISS_W1_G4_ARONDIGHT_SWORD_NO_W1_G4_FACTION_ORDER_W1_G4_WOLF_ARMOR_NOT_COMPLETED_W1_G5_ADDA_FATE_DONT_LIFT_CURSE','Configuration derived from: Сохранение 8+п(1020) (Орден, Трисс, Доспехи нет, Арондит нет, Д\'Иабль нет)',1),(105,'W1_CONFIG_W1_G3_ROMANCE_SHANI_W1_G4_ARONDIGHT_SWORD_YES_W1_G4_FACTION_ORDER_W1_G4_WOLF_ARMOR_COMPLETED_W1_G5_ADDA_FATE_LIFT_CURSE','Configuration derived from: Сохранение 9+п(1021) (Орден, Шани, Доспехи да, Арондит да, Д\'Иабль да)',1),(106,'W1_CONFIG_W1_G3_ROMANCE_SHANI_W1_G4_ARONDIGHT_SWORD_YES_W1_G4_FACTION_ORDER_W1_G4_WOLF_ARMOR_COMPLETED_W1_G5_ADDA_FATE_DONT_LIFT_CURSE','Configuration derived from: Сохранение 10+п(1022) (Орден, Шани, Доспехи да, Арондит да, Д\'Иабль нет)',1),(107,'W1_CONFIG_W1_G3_ROMANCE_SHANI_W1_G4_ARONDIGHT_SWORD_NO_W1_G4_FACTION_ORDER_W1_G4_WOLF_ARMOR_COMPLETED_W1_G5_ADDA_FATE_LIFT_CURSE','Configuration derived from: Сохранение 11+п(1023) (Орден, Шани, Доспехи да, Арондит нет, Д\'Иабль да)',1),(108,'W1_CONFIG_W1_G3_ROMANCE_SHANI_W1_G4_ARONDIGHT_SWORD_NO_W1_G4_FACTION_ORDER_W1_G4_WOLF_ARMOR_COMPLETED_W1_G5_ADDA_FATE_DONT_LIFT_CURSE','Configuration derived from: Сохранение 12+п(1024) (Орден, Шани, Доспехи да, Арондит нет, Д\'Иабль нет)',1),(109,'W1_CONFIG_W1_G3_ROMANCE_SHANI_W1_G4_ARONDIGHT_SWORD_YES_W1_G4_FACTION_ORDER_W1_G4_WOLF_ARMOR_NOT_COMPLETED_W1_G5_ADDA_FATE_LIFT_CURSE','Configuration derived from: Сохранение 13+п(1025) (Орден, Шани, Доспехи нет, Арондит да, Д\'Иабль да)',1),(110,'W1_CONFIG_W1_G3_ROMANCE_SHANI_W1_G4_ARONDIGHT_SWORD_YES_W1_G4_FACTION_ORDER_W1_G4_WOLF_ARMOR_NOT_COMPLETED_W1_G5_ADDA_FATE_DONT_LIFT_CURSE','Configuration derived from: Сохранение 14+п(1026) (Орден, Шани, Доспехи нет, Арондит да, Д\'Иабль нет)',1),(111,'W1_CONFIG_W1_G3_ROMANCE_SHANI_W1_G4_ARONDIGHT_SWORD_NO_W1_G4_FACTION_ORDER_W1_G4_WOLF_ARMOR_NOT_COMPLETED_W1_G5_ADDA_FATE_LIFT_CURSE','Configuration derived from: Сохранение 15+п(1027) (Орден, Шани, Доспехи нет, Арондит нет, Д\'Иабль да)',1),(112,'W1_CONFIG_W1_G3_ROMANCE_SHANI_W1_G4_ARONDIGHT_SWORD_NO_W1_G4_FACTION_ORDER_W1_G4_WOLF_ARMOR_NOT_COMPLETED_W1_G5_ADDA_FATE_DONT_LIFT_CURSE','Configuration derived from: Сохранение 16+п(1028) (Орден, Шани, Доспехи нет, Арондит нет, Д\'Иабль нет)',1);
/*!40000 ALTER TABLE `save_configurations` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_decisions`
--

DROP TABLE IF EXISTS `user_decisions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_decisions` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT 'ID пользователя, принявшего решение',
  `decision_point_id` bigint NOT NULL COMMENT 'ID точки решения',
  `chosen_choice_id` bigint NOT NULL COMMENT 'ID выбранного варианта выбора',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Дата и время фиксации решения',
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_user_decision_for_point` (`user_id`,`decision_point_id`),
  KEY `decision_point_id` (`decision_point_id`),
  KEY `chosen_choice_id` (`chosen_choice_id`),
  CONSTRAINT `user_decisions_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `user_decisions_ibfk_2` FOREIGN KEY (`decision_point_id`) REFERENCES `decision_points` (`id`) ON DELETE CASCADE,
  CONSTRAINT `user_decisions_ibfk_3` FOREIGN KEY (`chosen_choice_id`) REFERENCES `decision_choices` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=31 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Решения, принятые пользователями';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_decisions`
--

LOCK TABLES `user_decisions` WRITE;
/*!40000 ALTER TABLE `user_decisions` DISABLE KEYS */;
/*!40000 ALTER TABLE `user_decisions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_roles`
--

DROP TABLE IF EXISTS `user_roles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_roles` (
  `user_id` bigint NOT NULL,
  `role_id` bigint NOT NULL,
  PRIMARY KEY (`user_id`,`role_id`),
  KEY `role_id` (`role_id`),
  CONSTRAINT `user_roles_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `user_roles_ibfk_2` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Связь пользователей и ролей';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_roles`
--

LOCK TABLES `user_roles` WRITE;
/*!40000 ALTER TABLE `user_roles` DISABLE KEYS */;
INSERT INTO `user_roles` VALUES (2,1),(3,1),(4,1),(5,1),(1,2);
/*!40000 ALTER TABLE `user_roles` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(255) NOT NULL COMMENT 'Имя пользователя (логин)',
  `password_hash` varchar(255) NOT NULL COMMENT 'Хэш пароля пользователя',
  `email` varchar(255) NOT NULL COMMENT 'Адрес электронной почты пользователя',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Дата и время регистрации пользователя',
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`),
  UNIQUE KEY `email` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Таблица пользователей';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (1,'admin','$2a$10$x4pevUCB83siA8XqbSxvLOFKKlD.vB7wYcqC.yQMUJwaXPvdSscp2','jenia200202@mail.ru','2025-05-19 17:37:53'),(2,'user1','$2a$10$GTR7SQorvSKKIRlNdd799ObOIc9t8svrkCmJNBefTnsk/WlmgV7Wy','user1@mail.ru','2025-05-19 17:40:16'),(3,'new_witcher_user','$2a$10$IJ0K4JMcMVDTgqMom9mDvea/y1teIWfOJwxNFg/eYq926CaxCGD0.','new.user@example.com','2025-05-19 12:59:23'),(4,'LittlePunsh','$2a$10$QGoafMk1nYlJ3.4ZDHlvXeHSQvzBgl491HVXSn/KImE3o5svUXYhy','LittlePunsh@mail.ru','2025-05-19 13:15:30'),(5,'Pupok','$2a$10$oHM/GLBQ.nlmlAiqe3JkSu28PE/Kl4zoekaxUu3kY/I.6YM9fay9e','Pupok@mail.ru','2025-05-28 12:10:07');
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-05-29 13:30:48
