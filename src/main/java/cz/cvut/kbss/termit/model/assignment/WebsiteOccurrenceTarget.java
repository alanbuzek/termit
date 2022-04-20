package cz.cvut.kbss.termit.model.assignment;

import cz.cvut.kbss.jopa.model.annotations.OWLClass;
import cz.cvut.kbss.termit.model.resource.VocabularyMock;
import cz.cvut.kbss.termit.model.resource.Website;

/**
 * Target representing the content of a website.
 * <p>
 * The {@link #getSource()} value points to the identifier of the website.
 */
@OWLClass(iri = VocabularyMock.s_c_cil_weboveho_vyskytu)
public class WebsiteOccurrenceTarget extends OccurrenceTarget {
    public WebsiteOccurrenceTarget() {
    }

    public WebsiteOccurrenceTarget(Website source) {
        super(source);
    }

    @Override
    public String toString() {
        return "WebOccurrenceTarget{" + super.toString() + '}';
    }
}
