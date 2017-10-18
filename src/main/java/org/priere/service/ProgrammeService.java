package org.priere.service;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Named;
import javax.persistence.PersistenceUnitUtil;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;

import org.priere.domain.ProgrammeEntity;
import org.priere.domain.security.UserEntity;
import org.primefaces.model.SortOrder;

@Named
public class ProgrammeService extends BaseService<ProgrammeEntity> implements Serializable {

    private static final long serialVersionUID = 1L;
    
    public ProgrammeService(){
        super(ProgrammeEntity.class);
    }
    
    @Transactional
    public List<ProgrammeEntity> findAllProgrammeEntities() {
        
        return entityManager.createQuery("SELECT o FROM Programme o ", ProgrammeEntity.class).getResultList();
    }
    
    @Override
    @Transactional
    public long countAllEntries() {
        return entityManager.createQuery("SELECT COUNT(o) FROM Programme o", Long.class).getSingleResult();
    }
    
    @Override
    protected void handleDependenciesBeforeDelete(ProgrammeEntity programme) {

        /* This is called before a Programme is deleted. Place here all the
           steps to cut dependencies to other entities */
        
    }

    @Transactional
    public List<ProgrammeEntity> findAvailableProgrammes(UserEntity user) {
        return entityManager.createQuery("SELECT o FROM Programme o WHERE o.officiant IS NULL", ProgrammeEntity.class).getResultList();
    }

    @Transactional
    public List<ProgrammeEntity> findProgrammesByOfficiant(UserEntity user) {
        return entityManager.createQuery("SELECT o FROM Programme o WHERE o.officiant = :user", ProgrammeEntity.class).setParameter("user", user).getResultList();
    }

    @Transactional
    public List<ProgrammeEntity> findAvailableProgramme2s(UserEntity user) {
        return entityManager.createQuery("SELECT o FROM Programme o WHERE o.predicateur IS NULL", ProgrammeEntity.class).getResultList();
    }

    @Transactional
    public List<ProgrammeEntity> findProgramme2sByPredicateur(UserEntity user) {
        return entityManager.createQuery("SELECT o FROM Programme o WHERE o.predicateur = :user", ProgrammeEntity.class).setParameter("user", user).getResultList();
    }

    @Transactional
    public ProgrammeEntity lazilyLoadImageToProgramme(ProgrammeEntity programme) {
        PersistenceUnitUtil u = entityManager.getEntityManagerFactory().getPersistenceUnitUtil();
        if (!u.isLoaded(programme, "image") && programme.getId() != null) {
            programme = find(programme.getId());
            programme.getImage().getId();
        }
        return programme;
    }
    
    // This is the central method called by the DataTable
    @Override
    @Transactional
    public List<ProgrammeEntity> findEntriesPagedAndFilteredAndSorted(int firstResult, int maxResults, String sortField, SortOrder sortOrder, Map<String, Object> filters) {
        
        StringBuilder query = new StringBuilder();

        query.append("SELECT o FROM Programme o");
        
        // Can be optimized: We need this join only when officiant filter is set
        query.append(" LEFT OUTER JOIN o.officiant officiant");
        
        // Can be optimized: We need this join only when predicateur filter is set
        query.append(" LEFT OUTER JOIN o.predicateur predicateur");
        
        query.append(" LEFT JOIN FETCH o.image");
        
        String nextConnective = " WHERE";
        
        Map<String, Object> queryParameters = new HashMap<>();
        
        if (filters != null && !filters.isEmpty()) {
            
            nextConnective += " ( ";
            
            for(String filterProperty : filters.keySet()) {
                
                if (filters.get(filterProperty) == null) {
                    continue;
                }
                
                switch (filterProperty) {
                
                case "date":
                    query.append(nextConnective).append(" o.date = :date");
                    queryParameters.put("date", filters.get(filterProperty));
                    break;

                case "officiant":
                    query.append(nextConnective).append(" o.officiant = :officiant");
                    queryParameters.put("officiant", filters.get(filterProperty));
                    break;
                
                case "predicateur":
                    query.append(nextConnective).append(" o.predicateur = :predicateur");
                    queryParameters.put("predicateur", filters.get(filterProperty));
                    break;
                
                }
                
                nextConnective = " AND";
            }
            
            query.append(" ) ");
            nextConnective = " AND";
        }
        
        if (sortField != null && !sortField.isEmpty()) {
            query.append(" ORDER BY o.").append(sortField);
            query.append(SortOrder.DESCENDING.equals(sortOrder) ? " DESC" : " ASC");
        }
        
        TypedQuery<ProgrammeEntity> q = this.entityManager.createQuery(query.toString(), this.getType());
        
        for(String queryParameter : queryParameters.keySet()) {
            q.setParameter(queryParameter, queryParameters.get(queryParameter));
        }

        return q.setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }
    
}
