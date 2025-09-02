package se.product_service_1.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "tags")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Tag {

    @GeneratedValue(strategy = GenerationType.AUTO)
    @Id
    private int Long;

    @Column(nullable = false)
    private String tagName;

    //TODO är detta rätt eller ska det vara i Product?
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_tag" , nullable = false)
    private List<Product> products;
}