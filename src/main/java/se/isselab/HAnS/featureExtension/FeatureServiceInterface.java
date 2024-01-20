package se.isselab.HAnS.featureExtension;

import com.intellij.openapi.project.Project;
import se.isselab.HAnS.featureLocation.FeatureFileMapping;
import se.isselab.HAnS.featureLocation.FeatureLocation;
import se.isselab.HAnS.featureLocation.FeatureLocationBlock;
import se.isselab.HAnS.featureModel.psi.FeatureModelFeature;
import se.isselab.HAnS.fileHighlighter.FileHighlighter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
public interface FeatureServiceInterface {


    /**
     * Get Feature List from HAnS in Interface
     * @return
     */
    List<FeatureModelFeature> getFeatures();

    // &begin[FeatureFileMapping]
    FeatureFileMapping getFeatureFileMapping(FeatureModelFeature feature);
    void getFeatureFileMappingBackground(FeatureModelFeature feature, HAnSCallback callback);
    HashMap<String, FeatureFileMapping> getAllFeatureFileMappings();
    void getAllFeatureFileMappingsBackground(HAnSCallback callback);
    FeatureFileMapping getFeatureFileMappingOfFeature(HashMap<String, FeatureFileMapping> featureFileMappings, FeatureModelFeature feature);
    boolean isFeatureInFeatureFileMappings(HashMap<String, FeatureFileMapping> featureFileMappings, FeatureModelFeature feature);
    // &end[FeatureFileMapping]

    // &begin[LineCount]
    int getTotalFeatureLineCount(FeatureFileMapping featureFileMapping);
    int getFeatureLineCountInFile(FeatureFileMapping featureFileMapping, FeatureLocation featureLocation);
    // &end[LineCount]

    // &begin[Tangling]
    int getFeatureTangling(FeatureModelFeature feature);

    int getFeatureTangling(HashMap<String, FeatureFileMapping> fileMappings, FeatureModelFeature feature);
    HashSet<FeatureModelFeature> getTanglingMapOfFeature(HashMap<FeatureModelFeature, HashSet<FeatureModelFeature>> tanglingMap, FeatureModelFeature feature);
    void getTanglingMapBackground(HAnSCallback callback);

    void getFeatureTanglingBackground(FeatureModelFeature feature, HAnSCallback callback);

    HashMap<FeatureModelFeature, HashSet<FeatureModelFeature>> getTanglingMap();
    HashMap<FeatureModelFeature, HashSet<FeatureModelFeature>> getTanglingMap(HashMap<String, FeatureFileMapping> featureFileMappings);
    // &end[Tangling]

    // &begin[Scattering]
    int getFeatureScattering(FeatureModelFeature feature);

    int getFeatureScattering(FeatureFileMapping featureFileMapping);

    void getFeatureScatteringBackground(FeatureModelFeature feature, HAnSCallback callback);

    // &end[Scattering]

    // &begin[FeatureLocation]
    ArrayList<FeatureLocation> getFeatureLocations(FeatureFileMapping featureFileMapping);
    List<FeatureLocationBlock> getListOfFeatureLocationBlock(FeatureLocation featureLocation);
    // &end[FeatureLocation]
    // convenience methods
    List<FeatureModelFeature> getChildFeatures(FeatureModelFeature feature);
    FeatureModelFeature getParentFeature(FeatureModelFeature feature);

    /**
     * Retrieve root/first-level-features from feature
     * @param feature
     * @return
     */
    FeatureModelFeature getRootFeature(FeatureModelFeature feature);
    List<FeatureModelFeature> getRootFeatures();

    // &begin[FileHighlighter]
    void highlightFeatureInFeatureModel(String featureLpq);
    void highlighFeatureInFeatureModel(FeatureModelFeature feature);
    void openFileInProject(String path);
    void openFileInProject(String path, int startline, int endline);
    // &end[FileHighlighter]
    // CRUD feature methods
    void createFeature(FeatureModelFeature feature);

    // &begin[Referencing]
    /**
     * Rename feature of Feature Model. Updated feature will be returned on success. On failure old feature will be returned.
     * @param feature
     * @return
     */
    FeatureModelFeature renameFeature(FeatureModelFeature feature, String newName);
    boolean deleteFeature(FeatureModelFeature feature);
    // &end[Referencing]
    void getFeatureMetricsBackground(HAnSCallback callback);
}
