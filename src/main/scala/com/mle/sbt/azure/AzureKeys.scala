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
  val azureConf = settingKey[Path]("Path to Azure configuration file with account_name and account_key variables.")
  val azureContainerName = settingKey[String]("The Azure storage container name")
  val azureContainer = taskKey[StorageContainer]("Helper: Builds an Azure storage container. Used by other tasks.")
  val azurePackage = taskKey[Option[Path]]("Package to upload to Azure")
  val azureUpload = taskKey[java.net.URI]("Packages the app and uploads it to Azure Storage")
}

object AzureKeys extends AzureKeys
