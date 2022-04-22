package cz.cvut.kbss.termit.rest;

import cz.cvut.kbss.jsonld.JsonLd;
import cz.cvut.kbss.termit.dto.TermOccurrenceDTO;
import cz.cvut.kbss.termit.model.assignment.TermWebsiteOccurrence;
import cz.cvut.kbss.termit.model.assignment.WebsiteOccurrenceTarget;
import cz.cvut.kbss.termit.model.resource.Resource;
import cz.cvut.kbss.termit.model.resource.Website;
import cz.cvut.kbss.termit.model.selector.CssSelector;
import cz.cvut.kbss.termit.model.selector.Selector;
import cz.cvut.kbss.termit.model.selector.TextPositionSelector;
import cz.cvut.kbss.termit.model.selector.TextQuoteSelector;
import cz.cvut.kbss.termit.security.SecurityConstants;
import cz.cvut.kbss.termit.service.IdentifierResolver;
import cz.cvut.kbss.termit.service.business.ResourceService;
import cz.cvut.kbss.termit.service.business.TermOccurrenceService;
import cz.cvut.kbss.termit.util.Configuration;
import cz.cvut.kbss.termit.util.Constants;
import cz.cvut.kbss.termit.util.CsvUtils;
import cz.cvut.kbss.termit.util.Vocabulary;
import org.apache.jena.atlas.json.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(TermOccurrenceController.PATH)
public class TermOccurrenceController extends BaseController {

    private static final Logger LOG = LoggerFactory.getLogger(TermOccurrenceController.class);

    /**
     * URL path to this controller's endpoints.
     */
    public static final String PATH = "/occurrence";


    private final TermOccurrenceService occurrenceService;
    private final ResourceService resourceService;

    public TermOccurrenceController(IdentifierResolver idResolver, Configuration config,
                                    TermOccurrenceService occurrenceService,
                                    ResourceService resourceService) {
        super(idResolver, config);
        this.occurrenceService = occurrenceService;
        this.resourceService = resourceService;
    }

    @PutMapping(value = "/{normalizedName}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('" + SecurityConstants.ROLE_FULL_USER + "')")
    public void approveOccurrence(@PathVariable String normalizedName,
                                  @RequestParam(name = Constants.QueryParams.NAMESPACE) String namespace) {
        final URI identifier = idResolver.resolveIdentifier(namespace, normalizedName);

        occurrenceService.approve(occurrenceService.getRequiredReference(identifier));
        LOG.debug("Occurrence with identifier <{}> approved.", identifier);
    }

    @DeleteMapping(value = "/{normalizedName}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('" + SecurityConstants.ROLE_FULL_USER + "')")
    public void removeOccurrence(@PathVariable String normalizedName,
                                 @RequestParam(name = Constants.QueryParams.NAMESPACE) String namespace) {
        final URI identifier = idResolver.resolveIdentifier(namespace, normalizedName);
        occurrenceService.remove(occurrenceService.getRequiredReference(identifier));
        LOG.debug("Occurrence with identifier <{}> removed.", identifier);
    }

    @PostMapping(consumes = {MediaType.APPLICATION_JSON_VALUE, JsonLd.MEDIA_TYPE})
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('" + SecurityConstants.ROLE_FULL_USER + "')")
    public void createOccurrence(@RequestParam(name = Constants.QueryParams.NAMESPACE) String namespace,
                                 @RequestParam( required = false) Optional<String> termIdFragment,
                                 @RequestParam String websiteFragment,
                                 @RequestBody TermOccurrenceDTO termOccurrenceDTO
// TODO (alanb) uncomment this  @RequestBody TermWebsiteOccurrence termOccurrence
    ) {
        final URI websiteIdentifier =  idResolver.resolveIdentifier(namespace, websiteFragment);
        final Website website = (Website) resourceService.findRequired(websiteIdentifier);
        TermWebsiteOccurrence termWebsiteOccurrence = new TermWebsiteOccurrence();
        if (termIdFragment.isPresent()){
            final URI termUri = idResolver.resolveIdentifier(namespace, termIdFragment.get());
            termWebsiteOccurrence.setTerm(termUri);
//          TODO: add correct types
        }
        WebsiteOccurrenceTarget websiteOccurrenceTarget = new WebsiteOccurrenceTarget(website);
        HashSet<Selector> selectors = new HashSet<>();
        selectors.add(new CssSelector(termOccurrenceDTO.getSelector()));
        selectors.add(new TextQuoteSelector(termOccurrenceDTO.getExactMatch()));
        selectors.add(new TextPositionSelector(termOccurrenceDTO.getStart(), -1));
        websiteOccurrenceTarget.setSelectors(selectors);
        termWebsiteOccurrence.setTarget(websiteOccurrenceTarget);
        termWebsiteOccurrence.addType(Vocabulary.s_c_vyskyt_termu);
        occurrenceService.persist(termWebsiteOccurrence);

        LOG.debug("TermWebsiteOccurrence created: {}.", termWebsiteOccurrence);
    }


    @GetMapping(value = "/resources/{resourceFragment}",
                produces = {MediaType.APPLICATION_JSON_VALUE,
                            JsonLd.MEDIA_TYPE,
                            CsvUtils.MEDIA_TYPE,
                            Constants.Excel.MEDIA_TYPE,
                            Constants.Turtle.MEDIA_TYPE})
    public List<TermWebsiteOccurrence> getAllInResource(@RequestParam(name = Constants.QueryParams.NAMESPACE) String namespace,
                                                 @PathVariable String resourceFragment){

        final URI websiteIdentifier = idResolver.resolveIdentifier(namespace, resourceFragment);
        final Resource resource = resourceService.findRequired(websiteIdentifier);
        return occurrenceService.getAllOccurrencesInResource(resource);
    }
}
