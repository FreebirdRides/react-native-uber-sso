
Pod::Spec.new do |s|
  s.name         = "RNUberSSO"
  s.version      = "1.0.0"
  s.summary      = "RNUberSSO"
  s.description  = <<-DESC
                  RNUberSSO
                   DESC
  s.homepage     = "https://www.freebirdrides.com"
  s.license      = "MIT"
  # s.license      = { :type => "MIT", :file => "FILE_LICENSE" }
  s.author             = { "author" => "author@domain.cn" }
  s.platform     = :ios, "7.0"
  s.source       = { :git => "https://github.com/author/RNUberSSO.git", :tag => "master" }
  s.source_files  = "RNUberSSO/**/*.{h,m}"
  s.requires_arc = true


  s.dependency "React"
  #s.dependency "others"

end

  