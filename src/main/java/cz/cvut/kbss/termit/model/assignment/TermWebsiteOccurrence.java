package cz.cvut.kbss.termit.model.assignment;

import cz.cvut.kbss.jopa.model.annotations.OWLClass;
import cz.cvut.kbss.jopa.model.annotations.OWLDataProperty;
import cz.cvut.kbss.termit.model.resource.VocabularyMock;

import java.net.URI;
@OWLClass(iri = VocabularyMock.s_c_webovy_vyskyt_termu)
public class TermWebsiteOccurrence extends TermOccurrence  {

//    @OWLDataProperty(iri = VocabularyMock.s_p_poradi_vyskytu_termu)
//    private Integer orderNumber;
//
//    public Integer getOrderNumber() {
//        return orderNumber;
//    }
//
//    public void setOrderNumber(Integer orderNumber) {
//        this.orderNumber = orderNumber;
//    }

    public TermWebsiteOccurrence() {
    }

    public TermWebsiteOccurrence(URI term, WebsiteOccurrenceTarget target) {
        super(term, target);
    }

    @Override
    public WebsiteOccurrenceTarget getTarget() {
        assert target == null || target instanceof WebsiteOccurrenceTarget;
        return (WebsiteOccurrenceTarget) target;
    }

    public void setTarget(WebsiteOccurrenceTarget target) {
        this.target = target;
    }
}
