package com.mle.sbt.win

import xml.NodeSeq

/**
 * @author Michael
 */
case class ServiceFragments(components: NodeSeq, feature: NodeSeq)

object ServiceFragments {
  val Empty = ServiceFragments(NodeSeq.Empty, NodeSeq.Empty)

  def fromConf(conf: ServiceConf, displayName: String) = {
    val compsFragment: NodeSeq =
      (<Component Id='ServiceManagerConf' Guid='*'>
        <File Id={conf.confName.replace('.', '_')} Name={conf.confName} DiskId='1' Source={conf.confName}/>
      </Component>
        <Component Id='ServiceManager' Guid='*'>
          <File Id={conf.exeName} Name={conf.exeName} DiskId='1' Source={conf.serviceExe.toAbsolutePath.toString} KeyPath="yes"/>
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
          <ServiceControl Id="ServiceController" Start="install" Stop="both" Remove="uninstall" Name={displayName} Wait="yes"/>
        </Component>)
    val featureFragment: NodeSeq =
      (<Feature Id='InstallAsService'
                Title={"Install " + displayName + " as a Windows service"}
                Description={"This will install " + displayName + " as a Windows service."}
                Level='1'
                Absent='disallow'>
        <ComponentRef Id='ServiceManager'/>
        <ComponentRef Id='ServiceManagerConf'/>
      </Feature>)

    ServiceFragments(compsFragment, featureFragment)
  }
}