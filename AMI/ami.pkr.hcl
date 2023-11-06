variable "aws_region" {
  type    = string
  default = "us-east-1"
}

variable "source_ami" {
  type    = string
  default = "ami-06db4d78cb1d3bbf9" # Debian 12 (20230711-1438)
}

variable "ssh_username" {
  type    = string
  default = "admin"
}

variable "subnet_id" {
  type    = string
  default = "subnet-06f5a8307b2adeb81"
}

variable "ami_regions" {
  type = list(string)
  default = [
    "us-east-1",
  ]
}

variable "ami_prefix" {
  type    = string
  default = "CSYE"
}

variable "ami_user" {
  type    = string
  default = "494954498426"
}

variable "profile" {
  type    = string
  default = "dev"
}

locals { timestamp = regex_replace(timestamp(), "[- TZ:]", "") }

packer {
  required_plugins {
    amazon = {
      version = ">= 0.0.2"
      source  = "github.com/hashicorp/amazon"
    }
  }
}


source "amazon-ebs" "my-ami" {
  profile         = "${var.profile}"
  region          = "${var.aws_region}"
  ami_name        = "${var.ami_prefix}-${local.timestamp}"
  ami_description = "AMI for CSYE 6225 Cloud"
  ami_regions     = "${var.ami_regions}"
  ami_users       = ["${var.ami_user}"]
  #  ssh_keypair_name   = "id_rsa"
  #  ssh_private_key_file = "~/.ssh/id_rsa"

  aws_polling {
    delay_seconds = 120
    max_attempts  = 50
  }

  instance_type = "t2.micro"
  source_ami    = "${var.source_ami}"
  ssh_username  = "${var.ssh_username}"
  subnet_id     = "${var.subnet_id}"

  launch_block_device_mappings {
    delete_on_termination = true
    device_name           = "/dev/xvda"
    volume_size           = 25
    volume_type           = "gp2"
  }
}

build {
  sources = [
    "source.amazon-ebs.my-ami"
  ]

  provisioner "file" {
    source      = "../webapplication/target/webapplication-0.0.1-SNAPSHOT.jar"
    destination = "/home/admin/"
  }


  provisioner "file" {
    source      = "../webapplication/src/main/resources/opt/user.csv"
    destination = "/home/admin/"

  }

  provisioner "file" {
    source      = "cloudwatch-config.json"
    destination = "/home/admin/"
  }

  #    provisioner "shell" {
  #      inline = [
  #        "sudo touch /home/admin/application.properties",
  #        "sudo chmod 764 /home/admin/application.properties",
  #      ]
  #    }

  provisioner "shell" {
    scripts = ["setup.sh"]
    environment_vars = [
      "DEBIAN_FRONTEND=noninteractive",
      "CHECKPOINT_DISABLE=1"
    ]
  }

  provisioner "file" {
    source      = "myapp.service"
    destination = "/tmp/myapp.service"
  }

  provisioner "shell" {
    inline = [
      "sudo mv /tmp/myapp.service /etc/systemd/system/",
      "sudo systemctl enable myapp.service"
    ]
  }

  #  provisioner "file" {
  #    source      = "start.sh"
  #    destination = "/home/admin/"
  #  }
  #
  #  provisioner "shell" {
  #    inline = [
  #      "sudo mv /home/admin/start.sh /var/lib/cloud/scripts/per-boot/",
  #    "sudo chmod +x /var/lib/cloud/scripts/per-boot/start.sh"]
  #  }
  #destination = "/var/lib/cloud/scripts/per-boot/"
  post-processor "manifest" {
    output     = "manifest.json"
    strip_path = true
  }
}