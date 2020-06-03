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

import javax.ejb.LocalBean;
import javax.ejb.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.logging.Logger;

import static org.craigmcc.bookcase.model.Constants.ANTHOLOGY_NAME;
import static org.craigmcc.bookcase.model.Constants.AUTHOR_NAME;
import static org.craigmcc.bookcase.model.Constants.BOOK_NAME;
import static org.craigmcc.bookcase.model.Constants.MEMBER_NAME;
import static org.craigmcc.bookcase.model.Constants.SERIES_NAME;
import static org.craigmcc.bookcase.model.Constants.STORY_NAME;

/**
 * <p>Split out from {@link DevModeStartupService} so that service and integration
 * tests can call it separately if needed.</p>
 */
@LocalBean
@Singleton
public class DevModeDepopulateService {

    // Instance Variables ----------------------------------------------------

    @PersistenceContext
    private EntityManager entityManager;

    private static final Logger LOG =
            Logger.getLogger(DevModeDepopulateService.class.getSimpleName());

    // Public Methods --------------------------------------------------------

    public void depopulate() {
        LOG.info("----- Depopulate Development Test Data Begin -----");
        // depopulate data in order respecting dependencies
        depopulateStories();
        depopulateAnthologies();
        depopulateMembers();
        depopulateSeries();
        depopulateBooks();
        depopulateAuthors();
        depopulateMutatedModelEvents();
        // Restart the sequence generator since we are reloading data from scratch
        resetSequence();
        LOG.info("------ Depopulate Development Test Data End ------");
    }

    // Private Methods -------------------------------------------------------

    private void resetSequence() {
        entityManager.createNativeQuery("ALTER SEQUENCE hibernate_sequence RESTART WITH 1")
                .executeUpdate();
    }

    private void depopulateAnthologies() {
        int deletedCount = entityManager
                .createQuery("DELETE FROM " + ANTHOLOGY_NAME)
                .executeUpdate();
        LOG.info(String.format("Deleted %d anthologies", deletedCount));
    }

    private void depopulateAuthors() {
        int deletedCount = entityManager
                .createQuery("DELETE FROM " + AUTHOR_NAME)
                .executeUpdate();
        LOG.info(String.format("Deleted %d authors", deletedCount));
    }

    private void depopulateBooks() {
        int deletedCount = entityManager
                .createQuery("DELETE FROM " + BOOK_NAME)
                .executeUpdate();
        LOG.info(String.format("Deleted %d books", deletedCount));
    }

    private void depopulateMembers() {
        int deletedCount = entityManager
                .createQuery("DELETE FROM " + MEMBER_NAME)
                .executeUpdate();
        LOG.info(String.format("Deleted %d members", deletedCount));
    }

    private void depopulateMutatedModelEvents() {
        int deletedCount = entityManager
                .createNativeQuery("DELETE FROM mutatedModelEvents")
                .executeUpdate();
        LOG.info(String.format("Deleted %d mutated model events", deletedCount));
    }

    private void depopulateSeries() {
        int deletedCount = entityManager
                .createQuery("DELETE FROM " + SERIES_NAME)
                .executeUpdate();
        LOG.info(String.format("Deleted %d series", deletedCount));
    }

    private void depopulateStories() {
        int deletedCount = entityManager
                .createQuery("DELETE FROM " + STORY_NAME)
                .executeUpdate();
        LOG.info(String.format("Deleted %d stories", deletedCount));
    }

}
