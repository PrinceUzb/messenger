package uz.scala.messenger

import uz.scala.messenger.syntax.{ CirceSyntax, GenericSyntax, Http4Syntax }

object implicits extends GenericSyntax with CirceSyntax with Http4Syntax
