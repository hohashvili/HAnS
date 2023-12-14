package se.isselab.HAnS;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

import org.jetbrains.annotations.NotNull;

import se.isselab.HAnS.featureLocation.FeatureFileMapping;
import se.isselab.HAnS.featureLocation.FeatureLocationBlock;
import se.isselab.HAnS.featureLocation.FeatureLocationManager;
import se.isselab.HAnS.featureModel.FeatureModelUtil;
import se.isselab.HAnS.featureModel.psi.FeatureModelFeature;
import se.isselab.HAnS.featureModel.psi.FeatureModelFile;
import se.isselab.HAnS.featureModel.psi.impl.FeatureModelFeatureImpl;

public class TestClass extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {

        //TODO THESIS:
        // how to get project
        for(FeatureModelFeature feature : FeatureModelUtil.findFeatures(e.getProject())){
            FeatureFileMapping featureFileMapping = FeatureLocationManager.getFeatureFileMapping(feature);
            System.out.println("Feature: " + feature.getName());
            System.out.println("Test");

            if(feature.getParent() instanceof FeatureModelFile){
                System.out.println("Parent: " + ((FeatureModelFile)feature.getParent()).getName());
            }
            else {
                System.out.println("Parent: " + ((FeatureModelFeatureImpl) feature.getParent()).getName());
            }
            for(var child : feature.getChildren()){
                System.out.println("Child: " + ((FeatureModelFeatureImpl) child).getName());
            }


            for(String file : featureFileMapping.getAllFeatureLocations().keySet()){
                System.out.println("  File: " + file);

                for(FeatureLocationBlock featureLocationBlock : featureFileMapping.getAllFeatureLocations().get(file)){
                    System.out.println("    " + featureLocationBlock.toString());
                }

            }
        }
    }


}
