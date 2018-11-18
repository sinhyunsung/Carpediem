package edu.dongyang.cs.myapplication;

public class ItemList {
    public String name;
    public String phone;
    public boolean selected = false;

    public ItemList(String name, String phone, boolean selected){
        super();
        this.name = name;
        this.phone = phone;
        this.selected = selected;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

}
