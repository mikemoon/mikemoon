const RadioButtonGroup = (props) => {
    const [selectedIndex, setSelectChild] = useState(0);

    const selectedCallback=(id)=>{
        console.log("selectedCallback "+id)
        setSelectChild(id)
        //To do, type selected

    }

    return props.names.map((radioButtonName, index)=>(
        <RadioButton key={index} style={{marginLeft: (index > 0 ? 10 : 0)}} name={radioButtonName} 
            onSelectedCallBack={selectedCallback} id={index} selected={index == selectedIndex}/>
    ))
}

function RadioButton(props) {
    const [selected, setSelect] = useState(0);
    return (
        <TouchableOpacity onPress={()=>{
            setSelect(1)
            props.onSelectedCallBack(props.id)
            console.log('selected = '+selected)
        }}>
        <View style={{flexDirection:"row"}}>
            <View style={[{
            height: 24,
            width: 24,
            borderRadius: 12,
            borderWidth: 2,
            borderColor: (props.selected == true)  ? '#40e0d0' :'#000',
            alignItems: 'center',
            justifyContent: 'center',
            }, props.style]}>
            {
                (props.selected == true) ?
                <View style={{
                    height: 12,
                    width: 12,
                    borderRadius: 6,
                    backgroundColor: '#40e0d0',
                }}/>
                : null
            }
            </View>
            <Text style={{marginLeft:10}}>{props.name}</Text>
        </View>    
        </TouchableOpacity>
    );
  }
