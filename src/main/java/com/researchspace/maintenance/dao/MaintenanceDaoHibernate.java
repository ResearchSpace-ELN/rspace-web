package com.researchspace.maintenance.dao;

import com.researchspace.core.util.ISearchResults;
import com.researchspace.core.util.SearchResultsImpl;
import com.researchspace.dao.GenericDaoHibernate;
import com.researchspace.maintenance.model.ScheduledMaintenance;
import com.researchspace.model.PaginationCriteria;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.SimpleExpression;
import org.springframework.stereotype.Repository;

@Repository("maintenanceDao")
public class MaintenanceDaoHibernate extends GenericDaoHibernate<ScheduledMaintenance, Long>
    implements MaintenanceDao {

  public MaintenanceDaoHibernate() {
    super(ScheduledMaintenance.class);
  }

  public MaintenanceDaoHibernate(Class<ScheduledMaintenance> persistentClass) {
    super(persistentClass);
  }

  @Override
  public Optional<ScheduledMaintenance> getNextScheduledMaintenance() {
    PaginationCriteria<ScheduledMaintenance> pagination = new PaginationCriteria<>();
    pagination.setResultsPerPage(1);
    return Optional.ofNullable(getFutureMaintenances(pagination).getFirstResult());
  }

  @Override
  public List<ScheduledMaintenance> getAllFutureMaintenances() {
    return getAllFutureMaintenanceOrderedByDateAsc();
  }

  @Override
  @SuppressWarnings("unchecked")
  public ISearchResults<ScheduledMaintenance> getFutureMaintenances(
      PaginationCriteria<ScheduledMaintenance> pagination) {
    Date now = new Date();
    long total =
        ((Number)
                getSession()
                    .createCriteria(ScheduledMaintenance.class)
                    .add(Restrictions.gt("endDate", now))
                    .setProjection(Projections.rowCount())
                    .uniqueResult())
            .longValue();
    long firstResult = pagination.getPageNumber() * pagination.getResultsPerPage();
    if (firstResult > Integer.MAX_VALUE) {
      return new SearchResultsImpl<>(Collections.emptyList(), pagination, total);
    }
    List<ScheduledMaintenance> results =
        getSession()
            .createCriteria(ScheduledMaintenance.class)
            .add(Restrictions.gt("endDate", now))
            .addOrder(Order.asc("startDate"))
            .addOrder(Order.asc("id"))
            .setFirstResult((int) firstResult)
            .setMaxResults(pagination.getResultsPerPage())
            .list();
    return new SearchResultsImpl<>(results, pagination, total);
  }

  private List<ScheduledMaintenance> getAllFutureMaintenanceOrderedByDateAsc() {
    return doList(Restrictions.gt("endDate", new Date()));
  }

  @Override
  public List<ScheduledMaintenance> getOldMaintenances() {
    return doList(Restrictions.le("endDate", new Date()));
  }

  @SuppressWarnings("unchecked")
  private List<ScheduledMaintenance> doList(SimpleExpression dateRestriction) {
    return (List<ScheduledMaintenance>)
        getSession()
            .createCriteria(ScheduledMaintenance.class)
            .add(dateRestriction)
            .addOrder(Order.asc("startDate"))
            .addOrder(Order.asc("id"))
            .list();
  }
}
