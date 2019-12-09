import React, { Component } from "react";
import { Auth } from "aws-amplify";
import Row from "react-bootstrap/Row";
import Nav from "./Navbar";
import Chatbot from "./Chatbot";
import axios from 'axios';
import config from "../../src/config";
import { Redirect } from "react-router-dom";


const CURRENT_USER = 'currentUser'


export default class RegisterLicense extends Component {

  constructor(props) {
    super(props);
    this.state = {
      firstName: "",
      lastName: "",
      filesSelected: {},
      showMaxSizeError: false,
      fileSubmitted: false,
      isLoading: false
    };
  }


  stopReloading = () => {
    this.setState(() => ({
      isLoading: false,
    }));
  }

  myuploadFileToServer = (file) => {
    console.log("Uploading file to server")
    console.log(file.name)
    var size = file.size / 1024 / 1024 // 2MB Limit
    console.log("file size: " + size)
    this.setState(() => ({
      isLoading: true,
    }));
    if (size > 2) {
      this.setState(() => ({
        showMaxSizeError: true,
      }));
    } else { 

      var currentUser = localStorage.getItem(CURRENT_USER)
      var formData = new FormData()
      formData.append('username', currentUser)
      formData.append('file', file)

      const configure = {
        headers: {
          'content-type': 'multipart/form-data'
        }
      }

      axios.post(config.BackendUrl + '/identifications', formData, configure)
        .then(response1 => {
          console.log(response1)
          var id = response1.data.id
          axios.post(config.BackendUrl + '/verification/check/' + id)
            .then(response2 => {
              if (response2.data.result == 'PASS') {
                // newFormData.append('photo', response1.data.s3Key)
                // newFormData.append('isBlacklisted',response1.data.blacklisted)
                var apiGateWayUrl = "https://mnbpyxxh13.execute-api.us-east-1.amazonaws.com/dev/";
                axios.get(apiGateWayUrl + 'driverlicense?fileName='+response1.data.s3Key)
                  .then(response3 => {
                    console.log("success " + JSON.stringify(response3.data))
                    var {license, firstname, lastname, expiryDate} = response3.data.body
                    console.log("Trigger save lic call") 
                    var newFormData = new FormData()
                    newFormData.append("firstName",firstname)
                    newFormData.append("lastName",lastname)
                    newFormData.append("license",license)
                    newFormData.append("expiry",expiryDate)
                    axios.post(config.BackendUrl + '/license', newFormData)
                    .then(response4 => {
                      console.log("final response : "+ JSON.stringify(response4))
                      this.setState(() => ({
                        fileSubmitted: true,
                      }));
                    })
                    })
                    .catch(error => {
                      console.log(error)
                    })
                    .finally(() => {
                      this.stopReloading()
                    })
                  .catch(error => {
                    console.log(error)
                  })
                  .finally(() => {
                    this.stopReloading()
                  })
              }
              else {
                this.stopReloading()
                alert('Not allowed to book a car')
              }
            })
            .catch(error => {
              this.stopReloading()
              alert('Not allowed to book a car')
              console.log(error)
            })
      })
        .catch(error => {
          console.log(error)
          if(error.response.status === 500) {
            alert('Please upload a valid Driver License Image')
          }
          this.stopReloading()
        })
    }
  }

  onFileSubmit = (e) => {
    var file = this.state.filesSelected
    console.log("onFile Submitted :" + file)
    this.myuploadFileToServer(file)

  }

  fileDropped = (e) => {
    console.log("onFile onDropped" + e)
    var file = e.target.files[0];
    console.log("onFile onDropped image" + file)
    this.setState(() => ({
      filesSelected: file,
    }));
  }

  render() {
    var { isLoading } = this.state;
    console.log("register license render :" + this.state.fileSubmitted)
    if (this.state.fileSubmitted) {
     return (
        <div>
         <Nav />
          <React.Fragment>
             <h2>	
              {this.state.firstName}	
            </h2>	
            <div className="Card">	
              <p>Your Driver License is verified. Please go ahead and rent a car</p>	
            </div>	
          </React.Fragment>	
          <Chatbot />	
        </div>
       )
         }
    
    
    else {
      return (

        <div>
          <Nav />
          <React.Fragment>
            <br />
            <br />
            <Row className="justify-content-md-center">
              <br />
              <br />
              <h2>
                Hello  {this.state.firstName} , Please upload a valid license, before you proceed to book/rent a car
          </h2>
            </Row>
            <br />
            <br />
            <br />
            <Row className="justify-content-md-center">
              <div className="Card">
                <div id="filesubmit">
                  <input type="file" className="file-select" accept="image/*" onChange={this.fileDropped} />

                  <button className="file-submit"
                    onClick={this.onFileSubmit} disabled={isLoading} >
                    {isLoading && (
                      <i
                        className="fa fa-refresh fa-spin"
                        style={{ marginRight: "5px" }}
                      />
                    )}
                    {isLoading && <span>Please Wait</span>}
                    {!isLoading && <span>UPLOAD LICENSE</span>}
                  </button>

                </div>
              </div>
            </Row>
          </React.Fragment>
          <Chatbot />
        </div>
      )

    }
  }

  getUserData() {
    Auth.currentAuthenticatedUser()
      .then(data => {
        const { username, attributes } = data;
        const { name, family_name } = attributes
        console.log(username)
        console.log(name)

        var registeredUser = data.getUsername() + '_registered'
        localStorage.setItem(CURRENT_USER, data.getUsername())

        this.setState(prevState => ({
          firstName: name,
          lastName: family_name
        }));

        if (!localStorage.getItem(registeredUser)) {
          console.log("no key in local storage for user " + registeredUser)
          var userData = new Object();
          userData.username = data.getUsername()
          userData.firstname = name
          userData.lastname = family_name
          this.signInUser(userData)
        }
      })
      .catch(err => { console.log(err) })
  }

  componentDidMount() {
    console.log("component did mount : " + localStorage.getItem('amplify-authenticator-authState'))
    this.getUserData();
  }


}
