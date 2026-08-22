package com.audit.myexpense.controller;

import java.util.Arrays;
import java.util.Map;

import com.audit.myexpense.model.AssetDetails;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link AssetDetailController}.
 */
@ExtendWith(MockitoExtension.class)
class AssetDetailControllerTest {

    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private AssetDetailController controller;

    private static AssetDetails asset(String id, String name, String status) {
        AssetDetails details = new AssetDetails();
        details.id = id;
        details.name = name;
        details.status = status;
        details.type = "Gold";
        details.comments = "in locker";
        return details;
    }

    @Test
    void saveAssignsGeneratedIdAndInserts() {
        when(mongoTemplate.insert(any(AssetDetails.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AssetDetails request = asset(null, "Chain", "In-Locker");
        AssetDetails result = controller.saveAssetDetail(request);

        assertThat(result.id).isNotBlank();
        verify(mongoTemplate).insert(request);
    }

    @Test
    void fetchReturnsAllAssets() {
        AssetDetails stored = asset("a-1", "Ring", "In-use");
        when(mongoTemplate.findAll(AssetDetails.class)).thenReturn(Arrays.asList(stored));

        assertThat(controller.fetchAssetDetails()).containsExactly(stored);
    }

    @Test
    void deleteRemovesExistingAsset() {
        AssetDetails stored = asset("a-1", "Chain", "In-Locker");
        when(mongoTemplate.findById("a-1", AssetDetails.class)).thenReturn(stored);

        Map<String, Object> body = controller.deleteAsset("a-1");

        verify(mongoTemplate).remove(stored);
        String message = String.join(" ", body.values().stream().map(String::valueOf).toArray(String[]::new));
        assertThat(message).contains("Chain").contains("deleted successfully");
    }

    @Test
    void deleteOfUnknownAssetReportsNotFound() {
        when(mongoTemplate.findById("missing", AssetDetails.class)).thenReturn(null);

        Map<String, Object> body = controller.deleteAsset("missing");

        verify(mongoTemplate, never()).remove(any(AssetDetails.class));
        String message = String.join(" ", body.values().stream().map(String::valueOf).toArray(String[]::new));
        assertThat(message).contains("not found");
    }

    @Test
    void updateCopiesIncomingFieldsOntoStoredAsset() {
        AssetDetails stored = asset("a-1", "Old Name", "In-Loan/EMI");
        when(mongoTemplate.findById("a-1", AssetDetails.class)).thenReturn(stored);
        when(mongoTemplate.save(any(AssetDetails.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Map<String, Object> body = controller.updateAssetStatus(asset("a-1", "New Name", "In-Locker"));

        assertThat(stored.name).isEqualTo("New Name");
        assertThat(stored.status).isEqualTo("In-Locker");
        assertThat(stored.type).isEqualTo("Gold");
        assertThat(stored.comments).isEqualTo("in locker");
        assertThat(stored.updatedDate).isNotBlank();
        verify(mongoTemplate).save(stored);

        String message = String.join(" ", body.values().stream().map(String::valueOf).toArray(String[]::new));
        assertThat(message).contains("New Name").contains("updated successfully");
    }

    @Test
    void updateOfUnknownAssetReportsDataNotFound() {
        when(mongoTemplate.findById("ghost", AssetDetails.class)).thenReturn(null);

        Map<String, Object> body = controller.updateAssetStatus(asset("ghost", "Ghost", "Active"));

        verify(mongoTemplate, never()).save(any(AssetDetails.class));
        String message = String.join(" ", body.values().stream().map(String::valueOf).toArray(String[]::new));
        assertThat(message).contains("Data not found");
    }
}
