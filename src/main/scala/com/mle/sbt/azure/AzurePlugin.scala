package com.mle.sbt.azure

import java.nio.file.Path

import com.mle.azure.StorageClient
import com.mle.file.StorageFile
import com.mle.sbt.GenericPlugin
import com.mle.sbt.azure.AzureKeys._
import com.mle.util.Util
import sbt._

/**
 *
 * @author mle
 */
trait AzurePlugin extends Plugin {
  val azureSettings: Seq[Setting[_]] = Seq(
    azurePackage := None,
    azureConf := sbt.Path.userHome.toPath / "keys" / "azure-storage.sec",
    azureContainerName := "files",
    // Compilation error if attempting to use .value syntax here. hmm?
    azureContainer <<= (azureConf, azureContainerName) map ((conf, cont) => {
      val (account, key) = readCredentials(conf)
      val client = new StorageClient(account, key)
      client container cont
    })
  )

  private def readCredentials(file: Path) = {
    val credMap = Util.props(file.toAbsolutePath.toString)
    val accountName = credMap("account_name")
    val accountKey = credMap("account_key")
    (accountName, accountKey)
  }

  def describe = GenericPlugin.describe(
    azureUpload, azurePackage, azureConf, azureContainerName
  )
}

object AzurePlugin extends AzurePlugin