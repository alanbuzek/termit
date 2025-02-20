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
package cz.cvut.kbss.termit.service.document;

import cz.cvut.kbss.termit.exception.AnnotationGenerationException;
import cz.cvut.kbss.termit.model.AbstractTerm;
import cz.cvut.kbss.termit.model.Asset;
import cz.cvut.kbss.termit.model.assignment.OccurrenceTarget;
import cz.cvut.kbss.termit.model.assignment.TermOccurrence;
import cz.cvut.kbss.termit.model.resource.File;
import cz.cvut.kbss.termit.model.selector.Selector;
import cz.cvut.kbss.termit.persistence.dao.TermOccurrenceDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.List;
import java.util.Set;

/**
 * Creates annotations (term occurrences) for vocabulary terms.
 * <p>
 * The generated {@link TermOccurrence}s are assigned a special type so that it is clear they have been suggested by an
 * automated procedure and should be reviewed.
 */
@Service
public class AnnotationGenerator {

    private static final Logger LOG = LoggerFactory.getLogger(AnnotationGenerator.class);

    private final TermOccurrenceDao termOccurrenceDao;

    private final DocumentManager documentManager;

    private final TermOccurrenceResolvers resolvers;

    @Autowired
    public AnnotationGenerator(TermOccurrenceDao termOccurrenceDao,
                               DocumentManager documentManager,
                               TermOccurrenceResolvers resolvers) {
        this.termOccurrenceDao = termOccurrenceDao;
        this.documentManager = documentManager;
        this.resolvers = resolvers;
    }

    /**
     * Generates annotations (term occurrences) for terms identified in the specified document.
     *
     * @param content Content of file with identified term occurrences
     * @param source  Source file of the annotated document
     */
    @Transactional
    public void generateAnnotations(InputStream content, File source) {
        final TermOccurrenceResolver occurrenceResolver = findResolverFor(source);
        LOG.debug("Resolving annotations of file {}.", source);
        occurrenceResolver.parseContent(content, source);
        final List<TermOccurrence> occurrences = occurrenceResolver.findTermOccurrences();
        saveOccurrences(occurrences, source);
        saveAnnotatedContent(source, occurrenceResolver.getContent());
        LOG.trace("Finished generating annotations for file {}.", source);
    }

    private TermOccurrenceResolver findResolverFor(File file) {
        // This will allow us to potentially support different types of files
        final TermOccurrenceResolver htmlResolver = resolvers.htmlTermOccurrenceResolver();
        if (htmlResolver.supports(file)) {
            return htmlResolver;
        } else {
            throw new AnnotationGenerationException("Unsupported type of file " + file);
        }
    }

    private void saveOccurrences(List<TermOccurrence> occurrences, Asset<?> source) {
        LOG.trace("Saving term occurrences for asset {}.", source);
        final List<TermOccurrence> existing = termOccurrenceDao.findAllTargeting(source);
        occurrences.stream().filter(o -> isNew(o, existing))
                   .filter(o -> !o.getTerm().equals(source.getUri())).forEach(o -> {
                       o.addType(cz.cvut.kbss.termit.util.Vocabulary.s_c_navrzeny_vyskyt_termu);
                       termOccurrenceDao.persist(o);
                   });
    }

    /**
     * Checks whether the specified term occurrence is new or if there already exists an equivalent one.
     * <p>
     * Two occurrences are considered equivalent iff they represent the same term, they have a target with the same
     * source file, and the target contains at least one equal selector.
     *
     * @param occurrence The supposedly new occurrence to check
     * @param existing   Existing occurrences relevant to the specified file
     * @return Whether the occurrence is truly new
     */
    private static boolean isNew(TermOccurrence occurrence, List<TermOccurrence> existing) {
        final OccurrenceTarget target = occurrence.getTarget();
        assert target != null;
        final Set<Selector> selectors = target.getSelectors();
        for (TermOccurrence to : existing) {
            if (!to.getTerm().equals(occurrence.getTerm())) {
                continue;
            }
            final OccurrenceTarget fileTarget = to.getTarget();
            assert fileTarget != null;
            assert fileTarget.getSource().equals(target.getSource());
            // Same term, contains at least one identical selector
            if (fileTarget.getSelectors().stream().anyMatch(selectors::contains)) {
                LOG.trace("Skipping occurrence {} because another one with matching term and selectors exists.",
                        occurrence);
                return false;
            }
        }
        return true;
    }

    private void saveAnnotatedContent(File file, InputStream input) {
        documentManager.saveFileContent(file, input);
    }

    /**
     * Generates annotations (term occurrences) for terms identified in the specified term's definition.
     *
     * @param content       Term definition with identified term occurrences
     * @param annotatedTerm Term whose definition was annotated
     */
    @Transactional
    public void generateAnnotations(InputStream content, AbstractTerm annotatedTerm) {
        // We assume the content (text analysis output) is HTML-compatible
        final TermOccurrenceResolver occurrenceResolver = resolvers.htmlTermOccurrenceResolver();
        LOG.debug("Resolving annotations of the definition of {}.", annotatedTerm);
        occurrenceResolver.parseContent(content, annotatedTerm);
        final List<TermOccurrence> occurrences = occurrenceResolver.findTermOccurrences();
        saveOccurrences(occurrences, annotatedTerm);
        LOG.trace("Finished generating annotations for the definition of {}.", annotatedTerm);
    }
}
