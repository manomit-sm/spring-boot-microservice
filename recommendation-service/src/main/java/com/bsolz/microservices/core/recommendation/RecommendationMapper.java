package com.bsolz.microservices.core.recommendation;

import com.bsolz.api.core.recommendation.Recommendation;
import com.bsolz.microservices.core.recommendation.persistence.RecommendationEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RecommendationMapper {

    @Mapping(target = "rate", source="entity.rating")
    Recommendation entityToApi(RecommendationEntity entity);
    @Mapping(target = "rating", source="api.rate")
    RecommendationEntity apiToEntity(Recommendation api);
}
