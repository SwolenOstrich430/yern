package com.yern.service;

import com.yern.model.user.Role;
import com.yern.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoleService {

   @Autowired
   private RoleRepository roleRepository;

   public Role getRoleById(Long id) {
       return roleRepository.getRoleById(id);
   }

   public List<Role> getAllRoles() {
       return roleRepository.findAll();
   }

   public Role createRole(Role role) {
       return roleRepository.save(role);
   }
}