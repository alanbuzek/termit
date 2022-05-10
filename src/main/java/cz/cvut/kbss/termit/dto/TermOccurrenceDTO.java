/**
 * TermIt
 * Copyright (C) 2019 Czech Technical University in Prague
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package cz.cvut.kbss.termit.dto;

import java.util.ArrayList;
import java.util.List;

public class TermOccurrenceDTO {

    private String cssSelector;
    private String xPathSelector;
    private Integer start;
    private String exactMatch;
    private List<String> extraTypes = new ArrayList<>();
    private String id;
    private String termFragment;
    private String termNamespace;
    private String suggestedLemma;

    public TermOccurrenceDTO() {
    }

    public String getCssSelector() {
        return cssSelector;
    }

    public void setCssSelector(String cssSelector) {
        this.cssSelector = cssSelector;
    }

    public String getxPathSelector() {
        return xPathSelector;
    }

    public void setxPathSelector(String xPathSelector) {
        this.xPathSelector = xPathSelector;
    }

    public Integer getStart() {
        return start;
    }

    public void setStart(Integer start) {
        this.start = start;
    }

    public String getExactMatch() {
        return exactMatch;
    }

    public void setExactMatch(String exactMatch) {
        this.exactMatch = exactMatch;
    }

    public List<String> getExtraTypes() {
        return extraTypes;
    }

    public void setExtraTypes(List<String> extraTypes) {
        this.extraTypes = extraTypes;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTermFragment() {
        return termFragment;
    }

    public void setTermFragment(String termFragment) {
        this.termFragment = termFragment;
    }

    public String getTermNamespace() {
        return termNamespace;
    }

    public void setTermNamespace(String termNamespace) {
        this.termNamespace = termNamespace;
    }

    public String getSuggestedLemma() {
        return suggestedLemma;
    }

    public void setSuggestedLemma(String suggestedLemma) {
        this.suggestedLemma = suggestedLemma;
    }
}
