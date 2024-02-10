package com.bsolz.microservices.core.recommendation.services;

import com.bsolz.api.core.recommendation.Recommendation;
import com.bsolz.api.core.recommendation.RecommendationService;
import com.bsolz.api.exceptions.InvalidInputException;
import com.bsolz.util.http.ServiceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

@RestController
public class RecommendationServiceImpl implements RecommendationService {

    private static final Logger LOG = LoggerFactory.getLogger(RecommendationServiceImpl.class);

    private final ServiceUtil serviceUtil;

    public RecommendationServiceImpl(ServiceUtil serviceUtil) {
        this.serviceUtil = serviceUtil;
    }

    @Override
    public List<Recommendation> getRecommendation(int productId) {
        if (productId < 1) {
            throw new InvalidInputException("Invalid productId: " + productId);
        }

        if (productId == 113) {
            LOG.debug("No recommendations found for productId: {}", productId);
            return Collections.emptyList();
        }

        List<Recommendation> list = List.of(
            new Recommendation(productId, 1, "Author 1", 1, "Content 1", serviceUtil.getServiceAddress()),
            new Recommendation(productId, 2, "Author 2", 2, "Content 2", serviceUtil.getServiceAddress()),
            new Recommendation(productId, 3, "Author 3", 3, "Content 3", serviceUtil.getServiceAddress())
        );


        LOG.debug("/recommendation response size: {}", list.size());

        return list;
    }
}
