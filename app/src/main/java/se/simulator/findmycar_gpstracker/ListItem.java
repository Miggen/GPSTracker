package se.simulator.findmycar_gpstracker;

public class ListItem {
    private String id;
    private String name;

    public ListItem(){
        this.id = "";
        this.name = "";
    }

    public ListItem(String newId, String newName){
        this.id = newId;
        this.name = newName;
    }

    public void setId(String newId){
        this.id = newId;
    }

    public String getId(){
        return this.id;
    }

    public void setName(String newName){
        this.name = newName;
    }

    public String getName(){
        return this.name;
    }

    @Override
    public String toString(){
        return name;
    }

    public int compareTo(ListItem item){
        return name.length()-item.getName().length();
    }
}
