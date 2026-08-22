package com.audit.myexpense.controller;

import java.util.Map;

import com.audit.myexpense.model.ProfileDetail;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link ProfileDetailController}.
 */
@ExtendWith(MockitoExtension.class)
class ProfileDetailControllerTest {

    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private ProfileDetailController controller;

    @Test
    void saveForcesSingletonProfileIdAndSaves() {
        when(mongoTemplate.save(any(ProfileDetail.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProfileDetail request = new ProfileDetail();
        request.profileName = "Manikandan";

        ProfileDetail result = controller.profileDetail(request);

        assertThat(result.profileName).isEqualTo("Manikandan");
        assertThat(result.profileId).isEqualTo(1);
        assertThat(result.updatedDate).isNotBlank();
        verify(mongoTemplate).save(request);
    }

    @Test
    void fetchReturnsProfileWithIdOne() {
        ProfileDetail stored = new ProfileDetail();
        stored.profileName = "Manikandan";
        when(mongoTemplate.findOne(any(Query.class), eq(ProfileDetail.class))).thenReturn(stored);

        assertThat(controller.profileDetail()).isSameAs(stored);
    }

    @Test
    void fetchWithoutExistingProfileReturnsNull() {
        when(mongoTemplate.findOne(any(Query.class), eq(ProfileDetail.class))).thenReturn(null);

        assertThat(controller.profileDetail()).isNull();
    }
}
