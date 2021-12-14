package node;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

public class FileNode implements Serializable {
    String hash;
    String title;
    String[] keywords;
    String description;

    public FileNode(String hash, String title) {
        this.hash = hash;
        this.title = title;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FileNode fileNode = (FileNode) o;
        return Objects.equals(hash, fileNode.hash) && Objects.equals(title, fileNode.title) && Arrays.equals(keywords, fileNode.keywords) && Objects.equals(description, fileNode.description);
    }

    @Override
    public String toString() {
        return "[ hash -> " + hash + "]\n"+
                "[ title -> " + title  +"]\n"+
                "[ keywords -> " + Arrays.toString(keywords) +"]\n"+
                "[ description -> " + description + "]\n";
    }
}
