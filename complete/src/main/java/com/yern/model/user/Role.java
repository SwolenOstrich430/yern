//package com.yern.model.user;
//
//import com.fasterxml.jackson.annotation.JsonIgnore;
//import com.fasterxml.jackson.annotation.JsonProperty;
//import jakarta.persistence.*;
//import lombok.Getter;
//import lombok.Setter;
//
//import java.util.Set;
//
//// TODO:
//// * think about doing lineage/parent Id
//@Getter
//@Entity
//@Table(name = "roles")
//public class Role {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    @Column
//    private Long id;
//
//    @Basic
//    @Column
//    @Setter
//    private String name;
//
//    @Basic
//    @Column
//    @Setter
//    private String identifier;
//
//    @JoinColumn
//    @ManyToOne(
//            fetch = FetchType.LAZY
//    )
//    @Setter
//    private Role parent;
//
//    @OneToMany(fetch = FetchType.LAZY,mappedBy = "parent")
//    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
//    @Setter
//    private Set<Role> children;
//
//    @JsonIgnore
//    public Set<Role> getChildren() {
//        return children;
//    }
//
////    @JsonIgnore
////    Todo: Think about getting parents and full lineage
////    public Set<Role> getParents() {
////        return parent.getChildren();
////    }
//}
