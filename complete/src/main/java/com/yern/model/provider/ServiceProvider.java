//package com.yern.model.provider;
//
//import jakarta.persistence.*;
//import lombok.Getter;
//import lombok.Setter;
//
//@Entity
//@Table(name="service_providers")
//@Getter
//@Setter
//public class ServiceProvider {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    @Column(name="id")
//    private Integer id;
//
//    @Column
//    private String name;
//
//    @Column
//    private String description;
//
//    @Column
//    private String identifier;
//
//    @JoinColumn
//    @ManyToOne(fetch = FetchType.LAZY)
//    private ServiceProviderType serviceProviderType;
//}
