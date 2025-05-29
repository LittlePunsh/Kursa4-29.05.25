package org.kursplom.dataloader;

import java.util.List;


public class SaveDataJsonEntry {
    private String save_name;
    private List<String> choices;

    // Геттеры и Сеттеры
    public String getSave_name() { return save_name; }
    public void setSave_name(String save_name) { this.save_name = save_name; }
    public List<String> getChoices() { return choices; }
    public void setChoices(List<String> choices) { this.choices = choices; }
}