package cz.cvut.kbss.termit.service.business;

import cz.cvut.kbss.termit.dto.TermOccurrenceDTO;
import cz.cvut.kbss.termit.model.assignment.TermOccurrence;
import cz.cvut.kbss.termit.model.assignment.TermWebsiteOccurrence;
import cz.cvut.kbss.termit.model.resource.Resource;
import cz.cvut.kbss.termit.model.resource.Website;
import cz.cvut.kbss.termit.util.Configuration;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.util.List;

/**
 * Business service for managing {@link TermOccurrence}s.
 */
public interface TermOccurrenceService {

    /**
     * Gets a reference to a {@link TermOccurrence} with the specified identifier.
     * <p>
     * The returned instance may be empty apart from its identifier.
     *
     * @param id Term occurrence identifier
     * @return Matching term occurrence
     * @throws cz.cvut.kbss.termit.exception.NotFoundException If there is no such term occurrence
     */
    TermOccurrence getRequiredReference(URI id);

    @Transactional
    TermOccurrence find(URI id);

    /**
     * Persists the specified term occurrence.
     *
     * @param occurrence Occurrence to persist
     */
    void persist(TermOccurrence occurrence);

    /**
     * Approves the specified term occurrence.
     * <p>
     * This removes the suggested classification of the occurrence if it were present.
     *
     * @param occurrence Occurrence to approve
     */
    void approve(TermOccurrence occurrence);

    /**
     * Removes the specified term occurrence.
     *
     * @param occurrence Occurrence to remove
     */
    void remove(TermOccurrence occurrence);

    List<TermOccurrence> getAllOccurrencesInResource(Resource resource);


    @Transactional
    void removeAllInWebsite(Website website);

    @Transactional
    void removeAllSuggestionsInWebsite(Website website);

    List<TermWebsiteOccurrence> createWebOccurrences(List<TermOccurrenceDTO> termOccurrenceDTOs, Website website,
                                              String contextIri, Configuration.Namespace cfgNamespace);
}
