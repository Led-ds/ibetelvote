package com.br.ibetelvote.application.mapper;

import com.br.ibetelvote.application.auth.dto.UserProfileResponse;
import com.br.ibetelvote.domain.entities.User;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface AuthMapper {

    UserProfileResponse toUserProfileResponse(User user);
}