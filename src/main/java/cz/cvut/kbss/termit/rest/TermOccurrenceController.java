package cz.cvut.kbss.termit.rest;

import cz.cvut.kbss.jsonld.JsonLd;
import cz.cvut.kbss.termit.dto.TermOccurrenceDTO;
import cz.cvut.kbss.termit.exception.ValidationException;
import cz.cvut.kbss.termit.model.assignment.TermOccurrence;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
    protected final IdentifierResolver idResolver;


    public TermOccurrenceController(IdentifierResolver idResolver, Configuration config,
                                    TermOccurrenceService occurrenceService,
                                    ResourceService resourceService,
                                    IdentifierResolver idResolver1) {
        super(idResolver, config);
        this.occurrenceService = occurrenceService;
        this.resourceService = resourceService;
        this.idResolver = idResolver1;
    }

    @PutMapping(value = "/{normalizedName}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('" + SecurityConstants.ROLE_FULL_USER + "')")
    public void approveOccurrence(@PathVariable String normalizedName,
                                  @RequestParam String occurrenceNamespace,
                                  @RequestParam String termNamespace,
                                  @RequestParam(required = false) Optional<String> termFragment)
            throws URISyntaxException {
//        final URI identifier = idResolver.resolveIdentifier(namespace, normalizedName);
//       TODO: put real URI back
        final URI termOccurrenceIdentifier = idResolver.resolveIdentifier(occurrenceNamespace, normalizedName);
        TermOccurrence termOccurrence = occurrenceService.find(termOccurrenceIdentifier);
        System.out.println("TermOccurrence: " + termOccurrence);
        System.out.println("TARGET: " + termOccurrence.getTarget());
        if (termFragment.isPresent()){
            final URI termUri = idResolver.resolveIdentifier(termNamespace, termFragment.get());
            termOccurrence.setTerm(termUri);
        } else if (termFragment.isEmpty() && termOccurrence.getTerm() == null) {
            throw new ValidationException("Cannot approve a term occurrence without specifying what term it belongs to!");
        }
        occurrenceService.approve(termOccurrence);
        LOG.debug("Occurrence with identifier <{}> approved.", termOccurrenceIdentifier);
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
    @PreAuthorize("hasRole('" + SecurityConstants.ROLE_FULL_USER + "')")
    public List<TermWebsiteOccurrence> createWebOccurrences(
                                 @RequestParam String websiteNamespace,
                                 @RequestParam String websiteFragment,
                             @RequestParam(value = "contextIri", required = false) String contextIri,
                                 @RequestBody List<TermOccurrenceDTO> termOccurrenceDTOs
// TODO (alanb) uncomment this  @RequestBody TermWebsiteOccurrence termOccurrence
    ) {
        final Configuration.Namespace cfgNamespace = config.getNamespace();

        final URI websiteIdentifier =  idResolver.resolveIdentifier(websiteNamespace, websiteFragment);
        final Website website = (Website) resourceService.findRequired(websiteIdentifier);

        return termOccurrenceDTOs.stream().map(t -> createSingleWebOccurrence(t, website, contextIri, cfgNamespace)).collect(Collectors.toList());
    }

    private TermWebsiteOccurrence createSingleWebOccurrence(TermOccurrenceDTO termOccurrenceDTO, Website website, String contextIri, Configuration.Namespace cfgNamespace){
        URI termUri = null;
        if (termOccurrenceDTO.getTermFragment() != null && termOccurrenceDTO.getTermNamespace() != null){
            termUri = idResolver.resolveIdentifier(termOccurrenceDTO.getTermNamespace(), termOccurrenceDTO.getTermFragment());
//          TODO: add correct types
        }
        TermWebsiteOccurrence termWebsiteOccurrence = new TermWebsiteOccurrence();
        termWebsiteOccurrence.setTerm(termUri);
        WebsiteOccurrenceTarget websiteOccurrenceTarget = new WebsiteOccurrenceTarget(website);
        HashSet<Selector> selectors = new HashSet<>();
        selectors.add(new CssSelector(termOccurrenceDTO.getSelector()));
        selectors.add(new TextQuoteSelector(termOccurrenceDTO.getExactMatch()));
        selectors.add(new TextPositionSelector(termOccurrenceDTO.getStart(), -1));
        websiteOccurrenceTarget.setSelectors(selectors);
        termWebsiteOccurrence.setTarget(websiteOccurrenceTarget);
        termWebsiteOccurrence.addType(Vocabulary.s_c_vyskyt_termu);
        termWebsiteOccurrence.setUri(idResolver
                .generateDerivedIdentifier(URI.create(contextIri), cfgNamespace.getTermOccurrence().getSeparator(), termOccurrenceDTO.getId()));

//      TODO: should make sure not to add duplicate types
        if (!termOccurrenceDTO.getExtraTypes().isEmpty()){
            termWebsiteOccurrence.getTypes().addAll(termOccurrenceDTO.getExtraTypes());
        }
        occurrenceService.persist(termWebsiteOccurrence);
        LOG.debug("TermWebsiteOccurrence created: {}.", termWebsiteOccurrence);
        return termWebsiteOccurrence;
    }



    @GetMapping(value = "/resources/{resourceFragment}",
                produces = {MediaType.APPLICATION_JSON_VALUE,
                            JsonLd.MEDIA_TYPE,
                            CsvUtils.MEDIA_TYPE,
                            Constants.Excel.MEDIA_TYPE,
                            Constants.Turtle.MEDIA_TYPE})
    public List<TermOccurrence> getAllInResource(@RequestParam(name = Constants.QueryParams.NAMESPACE) String namespace,
                                                 @PathVariable String resourceFragment){

        final URI websiteIdentifier = idResolver.resolveIdentifier(namespace, resourceFragment);
        final Resource resource = resourceService.findRequired(websiteIdentifier);
        return occurrenceService.getAllOccurrencesInResource(resource);
    }
}
