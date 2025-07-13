package com.prolinkli.core.app.components.buildinfo.provider;

import com.prolinkli.core.app.components.buildinfo.model.BuildInfo;
import com.prolinkli.core.app.db.model.generated.BuildInfoDb;
import com.prolinkli.framework.abstractprovider.AbstractProvider;

public class BuildInfoProvider extends AbstractProvider<BuildInfoDb, BuildInfo> {

    @Override
    public void defineMap(ClassProviderBuilder mapper) {
        mapper.field("environment", "buildName");
        mapper.field("version", "buildVersion");
    }
  
}
