package com.malliina.sbt.win

import scala.xml.NodeSeq

/**
 * @author Michael
 */
case class ServiceFragments(components: NodeSeq, feature: NodeSeq)

object ServiceFragments {
  val Empty = ServiceFragments(NodeSeq.Empty, NodeSeq.Empty)

  def fromConf(conf: ServiceConf, displayName: String) = {
    val xmlName = conf.xmlConf.getFileName.toString
    val exeName = conf.exeName
    val exeConfigName = conf.exeConfigName
    val compsFragment: NodeSeq = {
      (
        <Component Id='ServiceManager' Guid='*'>
          <File Id={exeName}
                Name={exeName}
                DiskId='1'
                Source={conf.serviceExe.toAbsolutePath.toString}
                KeyPath="yes"/>
          <File Id={exeConfigName}
                Name={exeConfigName}
                DiskId='1'
                Source={conf.runtimeConf.toAbsolutePath.toString}
                KeyPath="no"/>
          <File Id={xmlName}
                Name={xmlName}
                DiskId='1'
                Source={conf.xmlConf.toAbsolutePath.toString}
                KeyPath="no"/>
          <ServiceInstall Id="ServiceInstaller"
                          Name={displayName}
                          DisplayName={displayName}
                          Description={"The " + displayName + " service"}
                          Type="ownProcess"
                          Vital="yes"
                          Start="auto"
                          Account="LocalSystem"
                          ErrorControl="normal"
                          Interactive="no"/>
          <ServiceControl Id="ServiceController"
                          Name={displayName}
                          Start="install"
                          Stop="both"
                          Remove="uninstall"
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
        <ComponentRef Id='ServiceManager'/>
      </Feature>)

//    under Feature: {exeConf.ref}{xmlConf.ref}
    ServiceFragments(compsFragment, featureFragment)
  }
}