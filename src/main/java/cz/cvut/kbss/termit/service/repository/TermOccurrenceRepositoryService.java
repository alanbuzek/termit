/**
 * TermIt
 * Copyright (C) 2019 Czech Technical University in Prague
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package cz.cvut.kbss.termit.service.repository;

import cz.cvut.kbss.termit.exception.NotFoundException;
import cz.cvut.kbss.termit.model.assignment.TermOccurrence;
import cz.cvut.kbss.termit.model.assignment.TermWebsiteOccurrence;
import cz.cvut.kbss.termit.model.resource.Resource;
import cz.cvut.kbss.termit.persistence.dao.TermOccurrenceDao;
import cz.cvut.kbss.termit.service.business.TermOccurrenceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.util.List;
import java.util.Objects;

import static cz.cvut.kbss.termit.util.Constants.SCHEDULING_PATTERN;

@Service
public class TermOccurrenceRepositoryService implements TermOccurrenceService {

    private static final Logger LOG = LoggerFactory.getLogger(TermOccurrenceRepositoryService.class);

    private final TermOccurrenceDao termOccurrenceDao;

    @Autowired
    public TermOccurrenceRepositoryService(TermOccurrenceDao termOccurrenceDao) {
        this.termOccurrenceDao = termOccurrenceDao;
    }

    @Override
    public TermOccurrence getRequiredReference(URI id) {
        return termOccurrenceDao.getReference(id).orElseThrow(() ->
                NotFoundException.create(TermOccurrence.class.getSimpleName(), id)
        );
    }

    @Transactional
    @Override
    public TermOccurrence find(URI id) {
        return termOccurrenceDao.find(id).orElseThrow(() ->
                NotFoundException.create(TermOccurrence.class.getSimpleName(), id)
        );
    }

    @Transactional
    @Override
    public void persist(TermOccurrence occurrence) {
        Objects.requireNonNull(occurrence);
        termOccurrenceDao.persist(occurrence);
    }

    @Transactional
    @Override
    public void approve(TermOccurrence occurrence) {
        Objects.requireNonNull(occurrence);
        LOG.trace("Approving term occurrence {}", occurrence);
        occurrence.removeType(cz.cvut.kbss.termit.util.Vocabulary.s_c_navrzeny_vyskyt_termu);
        termOccurrenceDao.update(occurrence);
    }

    @Transactional
    @Override
    public void remove(TermOccurrence occurrence) {
        Objects.requireNonNull(occurrence);
        LOG.trace("Removing term occurrence {}.", occurrence);
        termOccurrenceDao.remove(occurrence);
    }

    @Transactional
    @Override
    public List<TermWebsiteOccurrence> getAllOccurrencesInResource(Resource resource){
        return termOccurrenceDao.findAllTargetingWebsite(resource);
    }

    @Transactional
    @Override
    public void removeAllInResource(Resource resource){
        termOccurrenceDao.removeAllTargetingWebsite(resource);
    }

    /**
     * Cleans up possibly orphaned term occurrences.
     * <p>
     * Such occurrences reference targets whose sources no longer exist in the repository.
     */
    @Scheduled(cron = SCHEDULING_PATTERN)
    @Transactional
    public void cleanupOrphans() {
        LOG.debug("Executing orphaned term occurrences cleanup.");
        termOccurrenceDao.removeAllOrphans();
    }
}
