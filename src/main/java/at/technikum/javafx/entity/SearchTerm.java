package at.technikum.javafx.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import java.time.LocalDateTime;

@Entity
public class SearchTerm {

    @Id
    private String id;

    private String term;

    private LocalDateTime firstSearched;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public LocalDateTime getFirstSearched() {
        return firstSearched;
    }

    public void setFirstSearched(LocalDateTime firstSearched) {
        this.firstSearched = firstSearched;
    }
}
