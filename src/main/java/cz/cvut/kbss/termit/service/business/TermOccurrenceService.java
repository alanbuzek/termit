package cz.cvut.kbss.termit.service.business;

import cz.cvut.kbss.termit.model.assignment.TermOccurrence;
import cz.cvut.kbss.termit.model.assignment.TermWebsiteOccurrence;
import cz.cvut.kbss.termit.model.resource.Resource;

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

//    TODO: add comment
    List<TermWebsiteOccurrence> getAllOccurrencesInResource(Resource resource);
}
