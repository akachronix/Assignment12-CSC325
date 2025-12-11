package characters;

public abstract class Character {
    protected final String name;
    protected final String role;
    protected final int age;

    public Character(String name, String role, int age) {
        this.name = name;
        this.role = role;
        this.age = age;
    }

    public String getName() { return name; }
    public String getRole() { return role; }
    public int getAge() { return age; }

    @Override
    public String toString() { return name + (role != null ? " (" + role + ")" : "") + " Age: " + age; }
}
