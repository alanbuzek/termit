package cz.cvut.kbss.termit.model.assignment;

import cz.cvut.kbss.jopa.model.annotations.*;
import cz.cvut.kbss.termit.util.Vocabulary;

import java.net.URI;
@OWLClass(iri = Vocabulary.s_c_webovy_vyskyt_termu)
public class TermWebsiteOccurrence extends TermOccurrence  {


    public TermWebsiteOccurrence() {
    }

    public TermWebsiteOccurrence(URI term, WebsiteOccurrenceTarget target) {
        super(term, target);
    }

    @OWLDataProperty(iri = Vocabulary.s_p_ma_navrhovane_lemma)
    private String suggestedLemma;

    @Override
    public WebsiteOccurrenceTarget getTarget() {
        assert target == null || target instanceof WebsiteOccurrenceTarget;
        return (WebsiteOccurrenceTarget) target;
    }

    public void setTarget(WebsiteOccurrenceTarget target) {
        this.target = target;
    }

    public String getSuggestedLemma() {
        return suggestedLemma;
    }

    public void setSuggestedLemma(String suggestedLemma) {
        this.suggestedLemma = suggestedLemma;
    }
}
