package won.ecommerce.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Category {
    @Id
    @GeneratedValue
    private Long id;
    private String name;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;

    @OneToMany(mappedBy = "parent")
    private final List<Category> child = new ArrayList<>();

    @OneToMany(mappedBy = "id")
    private final List<Item> items = new ArrayList<>();

    public Category(String name) {
        this.name = name;
    }

    public void addParentCategory(Category category) {
        this.parent = category;
        category.child.add(this);
    }
}
