package com.github.vikthorvergara.spring.data.jpa.paging;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@SpringBootApplication
@EnableJpaRepositories(considerNestedRepositories = true)
public class JpaPagingPOC {

    @Entity(name = "Item")
    @Table(name = "items")
    public static class Item {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;
        private String name;
        private String category;
        private int price;

        public Item() {
        }

        public Item(String name, String category, int price) {
            this.name = name;
            this.category = category;
            this.price = price;
        }

        public Long getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getCategory() {
            return category;
        }

        public int getPrice() {
            return price;
        }

        @Override
        public String toString() {
            return "Item{" + id + ", " + name + ", " + category + ", $" + price + "}";
        }
    }

    public interface NameAndPrice {
        String getName();

        int getPrice();
    }

    public interface ItemRepository extends JpaRepository<Item, Long> {
        Page<Item> findByCategory(String category, Pageable pageable);

        List<NameAndPrice> findProjectedByCategory(String category, Sort sort);

        @Modifying
        @Query("update Item i set i.price = i.price + :delta where i.category = :category")
        int bulkRaisePrice(@Param("category") String category, @Param("delta") int delta);
    }

    @Service
    public static class Catalog {
        private final ItemRepository repo;

        public Catalog(ItemRepository repo) {
            this.repo = repo;
        }

        @Transactional
        public int raisePriceForCategory(String category, int delta) {
            return repo.bulkRaisePrice(category, delta);
        }
    }

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(JpaPagingPOC.class);
        app.setWebApplicationType(WebApplicationType.NONE);
        try (ConfigurableApplicationContext ctx = app.run(args)) {
            ItemRepository repo = ctx.getBean(ItemRepository.class);
            Catalog catalog = ctx.getBean(Catalog.class);

            seed(repo);
            pageableReturnsBoundedSlice(repo);
            sortDescendingByDifferentColumn(repo);
            interfaceProjectionSelectsOnlyChosenColumns(repo);
            modifyingQueryUpdatesMultipleRowsInOneStatement(catalog, repo);
        }
    }

    static void seed(ItemRepository repo) {
        repo.save(new Item("keyboard", "input", 130));
        repo.save(new Item("mouse", "input", 50));
        repo.save(new Item("trackball", "input", 95));
        repo.save(new Item("monitor", "display", 900));
        repo.save(new Item("ultrawide", "display", 1400));
        repo.save(new Item("desk-lamp", "lighting", 60));
    }

    static void pageableReturnsBoundedSlice(ItemRepository repo) {
        System.out.println("--- Pageable: page 0 size 2 sort by price asc, category=input ---");
        Page<Item> page = repo.findByCategory("input", PageRequest.of(0, 2, Sort.by("price").ascending()));
        System.out.println("totalElements=" + page.getTotalElements() + " totalPages=" + page.getTotalPages()
                + " number=" + page.getNumber() + " hasNext=" + page.hasNext());
        page.forEach(i -> System.out.println("  " + i));
    }

    static void sortDescendingByDifferentColumn(ItemRepository repo) {
        System.out.println("\n--- Pageable: page 1 size 2 sort by name desc, category=input ---");
        Page<Item> page = repo.findByCategory("input", PageRequest.of(1, 2, Sort.by("name").descending()));
        System.out.println("number=" + page.getNumber() + " content size=" + page.getNumberOfElements());
        page.forEach(i -> System.out.println("  " + i));
    }

    static void interfaceProjectionSelectsOnlyChosenColumns(ItemRepository repo) {
        System.out.println("\n--- interface projection NameAndPrice: only name + price, sorted ---");
        List<NameAndPrice> rows = repo.findProjectedByCategory("display", Sort.by("price").descending());
        rows.forEach(r -> System.out.println("  " + r.getName() + " $" + r.getPrice()));
    }

    static void modifyingQueryUpdatesMultipleRowsInOneStatement(Catalog catalog, ItemRepository repo) {
        System.out.println("\n--- @Modifying bulk update: raise input category by 10 ---");
        int updated = catalog.raisePriceForCategory("input", 10);
        System.out.println("rows affected = " + updated);
        repo.findByCategory("input", PageRequest.of(0, 10, Sort.by("price").ascending()))
                .forEach(i -> System.out.println("  " + i));
    }
}
