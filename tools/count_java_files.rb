# frozen_string_literal: true

require 'terminal-table'

DIALOGOS_DIR = File.expand_path('..', File.dirname(__FILE__)).freeze

result = {}
base_dir = Dir.glob(File.join(DIALOGOS_DIR, '*'))
              .reject { |file| file.include? File.join(DIALOGOS_DIR, 'plugins') }
plugins_dir = Dir.glob(File.join(DIALOGOS_DIR, 'plugins', '*'))

(base_dir + plugins_dir).each do |file|
  next unless File.directory? file
  next if file == 'plugins'
  short_name = File.basename(file).sub('DialogOS_', '')
  count = `find #{file} -name '*.java' | wc -l`.to_i
  loc = `( find #{file} -name '*.java' -print0 | xargs -0 cat ) | wc -l`.to_i
  result[short_name] = [count, loc] unless count.zero?
end

rows = result
       .sort_by { |_package, pair| -pair[0] }
       .map { |package, pair| [package, pair[0], pair[1]] }

puts Terminal::Table.new(headings: ['Package', '# Files', 'LoC'], rows: rows)
