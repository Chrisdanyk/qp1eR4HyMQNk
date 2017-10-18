package org.priere.service.security;

import java.io.Serializable;

import javax.inject.Named;
import javax.transaction.Transactional;

import org.priere.domain.security.RolePermission;
import org.priere.service.BaseService;

@Named
public class RolePermissionsService extends BaseService<RolePermission> implements Serializable {

    private static final long serialVersionUID = 1L;
    
    public RolePermissionsService(){
        super(RolePermission.class);
    }
    
    @Override
    @Transactional
    public long countAllEntries() {
        return entityManager.createQuery("SELECT COUNT(o) FROM RolePermission o", Long.class).getSingleResult();
    }

}
