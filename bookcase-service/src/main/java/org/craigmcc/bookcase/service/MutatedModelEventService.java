/*
 * Copyright 2020 craigmcc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.craigmcc.bookcase.service;

import org.craigmcc.bookcase.event.DeletedModelEvent;
import org.craigmcc.bookcase.event.InsertedModelEvent;
import org.craigmcc.bookcase.event.MutatedModelEvent;
import org.craigmcc.bookcase.event.UpdatedModelEvent;
import org.craigmcc.bookcase.exception.NotFound;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.enterprise.event.Observes;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

import static org.craigmcc.library.model.Constants.ID_COLUMN;

@LocalBean
@Stateless
public class MutatedModelEventService {

    // Instance Variables ----------------------------------------------------

    @PersistenceContext
    EntityManager entityManager;

    // Static Variables ------------------------------------------------------

    private static final Logger LOG = Logger.getLogger(MutatedModelEventService.class.getName());

    // Public Methods --------------------------------------------------------

    public @NotNull MutatedModelEvent find(@NotNull Long id) throws NotFound {
        MutatedModelEvent mutatedModelEvent = entityManager.find(MutatedModelEvent.class, id);
        if (mutatedModelEvent == null) {
            throw new NotFound(String.format("id: Missing mutated model event %d", id));
        }
        return mutatedModelEvent;
    }

    public @NotNull Collection<MutatedModelEvent> findAll() {

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<MutatedModelEvent> criteriaQuery =
                criteriaBuilder.createQuery(MutatedModelEvent.class);
        Root<MutatedModelEvent> from = criteriaQuery.from(MutatedModelEvent.class);

        CriteriaQuery<MutatedModelEvent> select = criteriaQuery.select(from);
        select.orderBy(criteriaBuilder.asc(from.get(ID_COLUMN)));
//        Can specify select.where() conditions as described below
        TypedQuery<MutatedModelEvent> typedQuery = entityManager.createQuery(select);
        List<MutatedModelEvent> results = typedQuery.getResultList();
        return results;

    }

    // Event Observer Methods ------------------------------------------------

    public void handleDeletedModel(@Observes DeletedModelEvent event) {
        LOG.info(String.format("handleDeletedModel(%s)", event.toString()));
        handleMutatedModel(event);
    }

    public void handleInsertedModel(@Observes InsertedModelEvent event) {
        LOG.info(String.format("handleInserteddModel(%s)", event.toString()));
        handleMutatedModel(event);
    }

    public void handleUpdatedModel(@Observes UpdatedModelEvent event) {
        LOG.info(String.format("handleUpdatedModel(%s)", event.toString()));
        handleMutatedModel(event);
    }

    // Private Methods -------------------------------------------------------

    private void handleMutatedModel(MutatedModelEvent mutatedModelEvent) {
        mutatedModelEvent.setPublished(LocalDateTime.now());
        mutatedModelEvent.setUpdated((mutatedModelEvent.getPublished()));
        entityManager.persist(mutatedModelEvent);
    }

}
