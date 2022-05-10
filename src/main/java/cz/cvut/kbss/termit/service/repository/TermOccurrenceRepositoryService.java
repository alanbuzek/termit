/**
 * TermIt Copyright (C) 2019 Czech Technical University in Prague
 * <p>
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with this program.  If not, see
 * <https://www.gnu.org/licenses/>.
 */
package cz.cvut.kbss.termit.service.repository;

import cz.cvut.kbss.termit.dto.TermOccurrenceDTO;
import cz.cvut.kbss.termit.exception.NotFoundException;
import cz.cvut.kbss.termit.model.assignment.TermOccurrence;
import cz.cvut.kbss.termit.model.assignment.TermWebsiteOccurrence;
import cz.cvut.kbss.termit.model.assignment.WebsiteOccurrenceTarget;
import cz.cvut.kbss.termit.model.resource.Resource;
import cz.cvut.kbss.termit.model.resource.Website;
import cz.cvut.kbss.termit.model.selector.*;
import cz.cvut.kbss.termit.persistence.dao.TermOccurrenceDao;
import cz.cvut.kbss.termit.service.IdentifierResolver;
import cz.cvut.kbss.termit.service.business.TermOccurrenceService;
import cz.cvut.kbss.termit.util.Configuration;
import cz.cvut.kbss.termit.util.Vocabulary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static cz.cvut.kbss.termit.util.Constants.SCHEDULING_PATTERN;
import static java.util.stream.Collectors.toList;

@Service
public class TermOccurrenceRepositoryService implements TermOccurrenceService {

    private static final Logger LOG = LoggerFactory.getLogger(TermOccurrenceRepositoryService.class);

    private final TermOccurrenceDao termOccurrenceDao;
    protected final IdentifierResolver idResolver;

    @Autowired
    public TermOccurrenceRepositoryService(TermOccurrenceDao termOccurrenceDao,
                                           IdentifierResolver idResolver) {
        this.termOccurrenceDao = termOccurrenceDao;
        this.idResolver = idResolver;
    }

    @Override
    public TermOccurrence getRequiredReference(URI id) {
        return termOccurrenceDao.getReference(id).orElseThrow(() -> NotFoundException.create(TermOccurrence.class, id));
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
        final TermOccurrence toApprove = termOccurrenceDao.find(occurrence.getUri()).orElseThrow(
                () -> NotFoundException.create(TermOccurrence.class, occurrence.getUri()));
        LOG.trace("Approving term occurrence {}", toApprove);
        toApprove.removeType(cz.cvut.kbss.termit.util.Vocabulary.s_c_navrzeny_vyskyt_termu);
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
    public List<TermOccurrence> getAllOccurrencesInResource(Resource resource){
        return termOccurrenceDao.findAllTargetingWebsite(resource);
    }

    @Transactional
    @Override
    public void removeAllInWebsite(Website website){
        termOccurrenceDao.removeAllTargetingWebsite(website);
    }

    @Transactional
    @Override
    public void removeAllSuggestionsInWebsite(Website website){
        termOccurrenceDao.removeAllSuggestionsTargetingWebsite(website);
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
    @Transactional
    @Override
    public List<TermWebsiteOccurrence> createWebOccurrences(List<TermOccurrenceDTO> termOccurrenceDTOs, Website website,
                                                            String contextIri, Configuration.Namespace cfgNamespace){
        return termOccurrenceDTOs.stream().map(t -> this.createWebOccurrence(t, website, contextIri, cfgNamespace)).collect(toList());
    }

    @Transactional
    public TermWebsiteOccurrence createWebOccurrence(TermOccurrenceDTO termOccurrenceDTO, Website website, String contextIri,
                                              Configuration.Namespace cfgNamespace){
        URI termUri = null;
        if (termOccurrenceDTO.getTermFragment() != null && termOccurrenceDTO.getTermNamespace() != null){
            termUri = idResolver.resolveIdentifier(termOccurrenceDTO.getTermNamespace(), termOccurrenceDTO.getTermFragment());
        }
        TermWebsiteOccurrence termWebsiteOccurrence = new TermWebsiteOccurrence();
        termWebsiteOccurrence.setTerm(termUri);
        WebsiteOccurrenceTarget websiteOccurrenceTarget = new WebsiteOccurrenceTarget(website);
        HashSet<Selector> selectors = new HashSet<>();
        selectors.add(new CssSelector(termOccurrenceDTO.getCssSelector()));
        selectors.add(new TextQuoteSelector(termOccurrenceDTO.getExactMatch()));
        selectors.add(new TextPositionSelector(termOccurrenceDTO.getStart(), -1));
        if (termOccurrenceDTO.getxPathSelector() != null){
            selectors.add(new XPathSelector(termOccurrenceDTO.getxPathSelector()));
        }
        websiteOccurrenceTarget.setSelectors(selectors);
        termWebsiteOccurrence.setTarget(websiteOccurrenceTarget);
        termWebsiteOccurrence.addType(Vocabulary.s_c_vyskyt_termu);
        termWebsiteOccurrence.setUri(idResolver
                .generateDerivedIdentifier(URI.create(contextIri), cfgNamespace.getTermOccurrence().getSeparator(), this.generateID()));

        if (!termOccurrenceDTO.getExtraTypes().isEmpty()){
            termWebsiteOccurrence.getTypes().addAll(termOccurrenceDTO.getExtraTypes());
        }
        if (termWebsiteOccurrence.getTypes().contains(Vocabulary.s_c_navrzeny_vyskyt_termu) && termOccurrenceDTO.getSuggestedLemma() != null){
            termWebsiteOccurrence.setSuggestedLemma(termOccurrenceDTO.getSuggestedLemma());
        }

        this.persist(termWebsiteOccurrence);
        LOG.debug("TermWebsiteOccurrence created: {}.", termWebsiteOccurrence);
        return termWebsiteOccurrence;
    }

    private String generateID() {
        UUID uuid = UUID.randomUUID();
        return uuid.toString().substring(0, 8).concat("-");
    }

}
