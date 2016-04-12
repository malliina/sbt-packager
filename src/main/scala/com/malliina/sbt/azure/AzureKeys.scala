package com.malliina.sbt.azure

import java.nio.file.Path

import com.malliina.azure.StorageContainer
import sbt._

/** Keys for uploading packaged artifacts to Azure Storage.
  */
trait AzureKeys {
  val azureConf = settingKey[Path]("Path to Azure configuration file with account_name and account_key variables.")
  val azureContainerName = settingKey[String]("The Azure storage container name")
  val azureContainer = taskKey[StorageContainer]("Helper: Builds an Azure storage container. Used by other tasks.")
  val azurePackage = taskKey[Option[Path]]("Package to upload to Azure")
  val azureUpload = taskKey[java.net.URI]("Packages the app and uploads it to Azure Storage")
}

object AzureKeys extends AzureKeys
