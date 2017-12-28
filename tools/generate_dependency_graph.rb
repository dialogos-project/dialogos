# frozen_string_literal: true

require 'graphviz'

@tred = false # set to true for transitive reduction of the graph

if `which tred`.empty?
  puts 'This script requires the GraphViz tool.'
  exit 0
end

DIALOGOS_DIR = File.expand_path('..', File.dirname(__FILE__)).freeze

build_files = Dir.glob(File.join(DIALOGOS_DIR, '**', 'build.gradle'))
                 .reject { |file| file.include? File.join('plugins', 'build.gradle') }
                 .reject { |file| file == File.join(DIALOGOS_DIR, 'build.gradle') }
                 .reject { |file| file.include? 'RealSpeak' }
                 .reject { |file| file.include? 'ATT' }
                 .reject { |file| file.include? 'example' }

graphviz = GraphViz.new(type: :digraph)

build_files.each do |file|
  project = file.split('/')[-2]
  graphviz.add_node(project)

  content = File.read(file)
  content.each_line do |line|
    # compile project(':com.clt.base')
    match = line.match(/[^\/]*project\w*\(?[\'\"]:(.*)[\'\"]/)
    next if match.nil?
    dependency = match.captures[0].to_s
    graphviz.add_node(dependency)
    graphviz.add_edge(project, dependency)
  end
end


if @tred
  Dir.mktmpdir do |dir|
    tmpfile = File.join(dir, 'dependencies.gv')
    tmpfile_reduced = File.join(dir, 'dependencies_reduced.gv')

    graphviz.output(dot: tmpfile)

    puts 'Performing transitive reduction...'
    `tred #{tmpfile} > #{tmpfile_reduced}` # tred performs a transitive reduction
    graphviz = GraphViz.parse(tmpfile_reduced)
  end
end

target = File.join(DIALOGOS_DIR, 'dependencies.png')
puts "Saving png to #{target}..."
graphviz.output(png: target)

puts 'Done.'
