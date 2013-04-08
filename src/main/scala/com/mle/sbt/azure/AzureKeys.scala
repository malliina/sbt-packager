package com.mle.sbt.azure

import sbt._
import java.nio.file.Path
import com.mle.azure.StorageContainer

/**
 * Keys for uploading packaged artifacts to Azure Storage.
 *
 * @author mle
 */
trait AzureKeys {
  val azureConf = SettingKey[Path]("azure-conf", "Path to Azure configuration file")
  val azureContainerName = SettingKey[String]("azure-container-name", "The Azure storage container name")
  val azureContainer = TaskKey[StorageContainer]("azure-container", "Helper: Builds an Azure storage container")
  val azurePackage = TaskKey[Option[Path]]("azure-package", "Package to upload to Azure")
  val uploadRelease = TaskKey[java.net.URI]("azure-upload", "Packages the app and uploads it to Azure Storage")
}

object AzureKeys extends AzureKeys
