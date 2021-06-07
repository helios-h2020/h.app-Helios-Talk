package eu.h2020.helios_social.happ.helios.talk.share;

import java.util.Objects;

public class ContextItem {

    private String id;
    private String name;
    private int color;

    public ContextItem(String id, String name, int color) {
        this.id = id;
        this.name = name;
        this.color = color;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public int getColor() {
        return color;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ContextItem that = (ContextItem) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
