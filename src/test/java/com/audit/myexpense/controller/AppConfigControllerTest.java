package com.audit.myexpense.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.audit.myexpense.model.AppConfig;
import com.audit.myexpense.model.Dropdown;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link AppConfigController}.
 */
@ExtendWith(MockitoExtension.class)
class AppConfigControllerTest {

    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private AppConfigController controller;

    @Test
    void savePersistsEveryConfigAndReturnsInput() {
        List<AppConfig> request = new ArrayList<>(Arrays.asList(
                new AppConfig("AssetTypes", "Movable,Non-Movable"),
                new AppConfig("Assets", "Gold,Silver,Property")));

        Collection<AppConfig> result = controller.appConfig(request);

        assertThat(result).isSameAs(request);
        verify(mongoTemplate, times(2)).save(any(AppConfig.class));
    }

    @Test
    void fetchReturnsAllConfigs() {
        List<AppConfig> stored = Arrays.asList(new AppConfig("AssetTypes", "Movable"));
        when(mongoTemplate.findAll(AppConfig.class)).thenReturn(stored);

        assertThat(controller.fetchAppConfigDetails()).containsExactlyElementsOf(stored);
    }

    @Test
    void dropDownSplitsConfiguredValuesIntoDropdowns() {
        when(mongoTemplate.findOne(any(Query.class), eq(AppConfig.class)))
                .thenReturn(new AppConfig("InvestmentStatus", "Active,Closed"));

        List<Dropdown> result = controller.fetchAppDropDowns("InvestmentStatus");

        assertThat(result).extracting(Dropdown::getId).containsExactly("Active", "Closed");
        assertThat(result).allSatisfy(dropdown -> assertThat(dropdown.value).isEqualTo(dropdown.id));
    }

    @Test
    void dropDownForUnknownKeyReturnsEmptyList() {
        when(mongoTemplate.findOne(any(Query.class), eq(AppConfig.class)))
                .thenReturn(null);

        assertThat(controller.fetchAppDropDowns("Unknown")).isEmpty();
    }

    @Test
    void deleteRemovesExistingConfig() {
        AppConfig stored = new AppConfig("AssetTypes", "Movable,Non-Movable");
        when(mongoTemplate.findById("AssetTypes", AppConfig.class)).thenReturn(stored);

        Map<String, Object> body = controller.deleteAppConfig("AssetTypes");

        verify(mongoTemplate).remove(stored);
        String message = String.join(" ", body.values().stream().map(String::valueOf).toArray(String[]::new));
        assertThat(message).contains("AssetTypes").contains("deleted");
    }

    @Test
    void deleteOfUnknownConfigReportsNotFound() {
        when(mongoTemplate.findById("missing", AppConfig.class)).thenReturn(null);

        Map<String, Object> body = controller.deleteAppConfig("missing");

        verify(mongoTemplate, never()).remove(any(AppConfig.class));
        String message = String.join(" ", body.values().stream().map(String::valueOf).toArray(String[]::new));
        assertThat(message).contains("not found");
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void defaultConfigInsertsTheFullDefaultCatalogue() {
        Collection<AppConfig> result = controller.defaultConfig();

        ArgumentCaptor<Collection> captor = ArgumentCaptor.forClass(Collection.class);
        verify(mongoTemplate).insertAll(captor.capture());
        Collection<AppConfig> inserted = captor.getValue();

        assertThat(inserted).hasSameSizeAs(result);
        assertThat(result).extracting(config -> config.key)
                .containsExactly("AssetTypes", "Assets", "AssetStatus", "Investment",
                        "InvestmentStatus", "InsurenceType", "EventType");
        assertThat(new ArrayList<>(result).get(0).value).isEqualTo("Movable,Non-Movable");
    }
}
