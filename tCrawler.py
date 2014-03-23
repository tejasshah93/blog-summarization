from bs4 import BeautifulSoup
import urllib2, json
import os, errno

# Function to check whether the path exists or not
def make_sure_path_exists(path):
    try:
        os.makedirs(path)
    except OSError as exception:
        if exception.errno != errno.EEXIST:
            raise

# List of TechCrunch URLs for crawling 
URLs=[
"http://techcrunch.com/2014/03/02/mashable-14-m-series-a/",
"http://techcrunch.com/2014/03/20/google-wants-everyone-to-stop-hating-on-glass/"
]

# List of TechCrunch URLs for getting the comments using Facebook Graph API
graphURLs=[
"https://graph.facebook.com/comments/?limit=500&ids=http://techcrunch.com/2014/03/02/mashable-14-m-series-a/",
"https://graph.facebook.com/comments/?limit=500&ids=http://techcrunch.com/2014/03/20/google-wants-everyone-to-stop-hating-on-glass/"
]

i = len(URLs)
j = 0
datasetPath = "./dataset"

print "Let the crawling begin!\nTotal no. of blogs = " + str(i)

while j < i:
	netDirPath = os.path.join(datasetPath, "Blog " + str(j+1))
	make_sure_path_exists(netDirPath)

	# blogFile contains all the blogs with title, body etc.
	netBlogPath = os.path.join(netDirPath, "blogDataset.json")
	if os.path.exists(netBlogPath):
		os.remove(netBlogPath)
	blogFile = open(netBlogPath, 'w')

	# commentsFile contains all the comments
	netCommentsPath = os.path.join(netDirPath, "commentsDataset.json")
	if os.path.exists(netCommentsPath):
		os.remove(netCommentsPath)
	commentsFile = open(netCommentsPath, 'w')

	blogFile.write('{\n\t"blog" : {\n')
	#commentsFile.write('{\n\t"comments" : [\n')
	
	responseComments = urllib2.urlopen(graphURLs[j])
	dataComments = json.loads(responseComments.read())
	commentsFile.write((json.dumps(dataComments, indent = 2)))

	# if j == i-1:
	# 	commentsFile.write('\n\t]\n}')
	# else:
	# 	commentsFile.write(',\n')
	
	responseBlog = urllib2.urlopen(URLs[j])
	html = responseBlog.read()
	soup = BeautifulSoup(html)

	blogFile.write('\n\t\t\t"url": "' + URLs[j] + '",\n' + '\t\t\t"title": "' + soup.h1.contents[0] + '",\n')

	for links in soup.find_all('div'):
		if links.get('class')!=None:
		  if links.get('class')[0]=="title-left":
		  	dateAuthor = links.get_text()
			dateAuthor = dateAuthor.strip().split(' ')			
			#print dateAuthor
			indexBy = dateAuthor.index("by")
			k = 1
			blogDateParts = ""
			while k < indexBy:
				blogDateParts += dateAuthor[k] + " "
				k = k + 1
			blogDate = blogDateParts

			blogAuthorList = dateAuthor[indexBy + 2].split('\n')
			blogAuthorLastName = ""
			nextStr = 0
			while nextStr < len(blogAuthorList):				
				if len(blogAuthorList[nextStr]) != 0 and "More" not in blogAuthorList[nextStr]:
					blogAuthorLastName = blogAuthorList[nextStr]
				nextStr = nextStr + 1
			blogAuthor = dateAuthor[indexBy + 1]+ ' ' + blogAuthorLastName

			blogFile.write('\t\t\t"date" : "' + blogDate + '",\n')
			blogFile.write('\t\t\t"author" : "' + blogAuthor + '",\n')

	
	blogBody = ""
	for links in soup.find_all('p'):
			blogBodyPart = links.get_text().strip()
			if len(blogBodyPart) > 0 and blogBodyPart != "More" and not "Latest headlines delivered to you daily" in blogBodyPart:
				blogBody += blogBodyPart.encode('utf-8' + '\n')
	blogFile.write('\t\t\t"body" : "' + blogBody + '"\n')			
	blogFile.write('\t}\n}')

	# if j == i-1:
	# 	blogFile.write('\t\t}\n')
	# else:
	# 	blogFile.write('\t\t},\n')

	blogFile.close()
	commentsFile.close()

	j = j + 1
	print "Blog " + str(j) + " done"

print "Cool. Done with Blogs and Comments!\nblogDataset.json => blog title, date, author, body\ncommentsDataset.json => comments from graph API"

print "\nOnto the replies part now..."


i=len(URLs)
j=0
path = "replies"

while j < i:
	print "Getting replies for all the comments in Blog " + str(j + 1) + " " + URLs[j]
	# Reading commentsDataset to get the replies for each comment
	netDirPath = os.path.join(datasetPath, "Blog " + str(j+1))
	netCommentsPath = os.path.join(netDirPath, "commentsDataset.json")
	commentsFile = open(netCommentsPath,'r')
	commentsJSON = json.load(commentsFile)

	# In "replies" folder, a separate folder for each blog will be created
	# which will contain replies of its respective comments with name as commentID
	commentsIDs = []
	numberOfComments = len(commentsJSON[URLs[j]]["comments"]["data"])
	for k in range(numberOfComments):
		commentsIDs.append(commentsJSON[URLs[j]]["comments"]["data"][k]["id"].encode('ascii'))

	for commentsID in commentsIDs:
		# Creating a replies file for each comment with name as commentID
		netReplyPath = os.path.join(netDirPath, path)
		make_sure_path_exists(netReplyPath)
		repliesFileName = 'replyFor-' + commentsID + '.json'
		netPathRepliesFile = os.path.join(netReplyPath, repliesFileName)

		if os.path.exists(netPathRepliesFile):
			os.remove(netPathRepliesFile)

		repliesFile = open((netPathRepliesFile), 'w')
		repliesURL = "https://graph.facebook.com/" + commentsID + "/comments/?limit=500"
		responseReplies = urllib2.urlopen(repliesURL)
		dataReplies = json.loads(responseReplies.read())
		repliesFile.write((json.dumps(dataReplies, indent = 2)))
		repliesFile.close()

	commentsFile.close()
	j = j + 1

print "Done! Check the 'replies' folder"