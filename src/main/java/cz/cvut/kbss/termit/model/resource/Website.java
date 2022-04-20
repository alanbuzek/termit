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
package cz.cvut.kbss.termit.model.resource;

import com.fasterxml.jackson.annotation.JsonBackReference;
import cz.cvut.kbss.jopa.model.annotations.*;
import cz.cvut.kbss.jopa.vocabulary.DC;
import cz.cvut.kbss.jsonld.annotation.JsonLdAttributeOrder;
import cz.cvut.kbss.termit.util.Vocabulary;

import java.util.Objects;
import java.util.Set;

@OWLClass(iri = VocabularyMock.s_c_webova_stranka)
@JsonLdAttributeOrder({"uri", "label", "description"})
public class Website extends Resource {
    public Website() {
    }

    public Website(Document document, String url) {
        this.document = document;
        this.url = url;
    }

    @JsonBackReference
    @Inferred
    @OWLObjectProperty(iri = Vocabulary.s_p_je_casti_dokumentu, fetch = FetchType.EAGER)
    private Document document;

    @Types
    private Set<String> types;

//    TODO: adjust as needed
    @OWLDataProperty(iri = DC.Terms.LOCATION)
    private String url;

    public Document getDocument() {
        return document;
    }

    public void setDocument(Document document) {
        this.document = document;
    }

    public Set<String> getTypes() {
        return types;
    }

    public void setTypes(Set<String> types) {
        this.types = types;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Website)) {
            return false;
        }
        Website file = (Website) o;
        return Objects.equals(getUri(), file.getUri());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUri());
    }

    @Override
    public String toString() {
        return "File{" +
                super.toString() + (document != null ? "document=<" + document.getUri() + ">" : "") + '}';
    }
}
