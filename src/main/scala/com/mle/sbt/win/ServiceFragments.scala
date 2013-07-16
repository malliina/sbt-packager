package com.mle.sbt.win

import xml.NodeSeq
import com.mle.sbt.WixUtils

/**
 * @author Michael
 */
case class ServiceFragments(components: NodeSeq, feature: NodeSeq)

object ServiceFragments {
  val Empty = ServiceFragments(NodeSeq.Empty, NodeSeq.Empty)

  def fromConf(conf: ServiceConf, displayName: String) = {
    val exeConfigName = conf.exeName + ".config"
    // this and the confFile are written during packaging
    val xmlConf = WixUtils.wixify(conf.confName)
    val exeConf = WixUtils.wixify(exeConfigName)

    val compsFragment: NodeSeq = {
      xmlConf.comp ++ exeConf.comp ++ (
        <Component Id='ServiceManager' Guid='*'>
          <File Id={conf.exeName}
                Name={conf.exeName}
                DiskId='1'
                Source={conf.serviceExe.toAbsolutePath.toString}
                KeyPath="yes"/>
          <ServiceInstall Id="ServiceInstaller"
                          Type="ownProcess"
                          Vital="yes"
                          Name={displayName}
                          DisplayName={displayName}
                          Description={"The " + displayName + " service"}
                          Start="auto"
                          Account="LocalSystem"
                          ErrorControl="ignore"
                          Interactive="no"/>
          <ServiceControl Stop="both"
                          Remove="uninstall"
                          Id="ServiceController"
                          Start="install"
                          Name={displayName}
                          Wait="yes"/>
        </Component>
        )
    }

    val featureFragment: NodeSeq =
      (<Feature Id='InstallAsService'
                Title={"Install " + displayName + " as a Windows service"}
                Description={"This will install " + displayName + " as a Windows service."}
                Level='1'
                Absent='disallow'>
        <ComponentRef Id='ServiceManager'/>{xmlConf.ref}{exeConf.ref}
      </Feature>)

    ServiceFragments(compsFragment, featureFragment)
  }
}